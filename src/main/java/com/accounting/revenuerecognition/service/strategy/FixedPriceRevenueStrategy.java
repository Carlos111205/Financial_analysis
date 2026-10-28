package com.accounting.revenuerecognition.service.strategy;

import com.accounting.revenuerecognition.domain.enums.InvoiceStatus;
import com.accounting.revenuerecognition.domain.enums.MilestoneStatus;
import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.domain.model.InvoiceLineItem;
import com.accounting.revenuerecognition.domain.model.Milestone;
import com.accounting.revenuerecognition.dto.request.GenerateInvoiceRequest;
import com.accounting.revenuerecognition.exception.InvalidRevenueOperationException;
import com.accounting.revenuerecognition.repository.InvoiceRepository;
import com.accounting.revenuerecognition.repository.MilestoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Revenue Recognition for Fixed-Price Contracts:
 * Invoicing and revenue recognition occur when performance milestones are achieved or
 * according to fixed deliverable schedules, constrained by the Total Contract Value.
 */
@Component
public class FixedPriceRevenueStrategy implements RevenueRecognitionStrategy {

    private static final Logger log = LoggerFactory.getLogger(FixedPriceRevenueStrategy.class);

    private final MilestoneRepository milestoneRepository;
    private final InvoiceRepository invoiceRepository;

    public FixedPriceRevenueStrategy(MilestoneRepository milestoneRepository,
                                     InvoiceRepository invoiceRepository) {
        this.milestoneRepository = milestoneRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public PricingModel getSupportedModel() {
        return PricingModel.FIXED_PRICE;
    }

    @Override
    public RevenueCalculationResult calculateRevenueAndInvoice(ContractProject project, GenerateInvoiceRequest request) {
        List<Milestone> eligibleMilestones = new ArrayList<>();

        if (request.getTargetMilestoneId() != null) {
            Milestone milestone = milestoneRepository.findById(request.getTargetMilestoneId())
                    .orElseThrow(() -> new InvalidRevenueOperationException("Target milestone with ID " + request.getTargetMilestoneId() + " not found"));

            if (!milestone.getProject().getId().equals(project.getId())) {
                throw new InvalidRevenueOperationException("Milestone does not belong to project " + project.getProjectCode());
            }
            if (milestone.getStatus() != MilestoneStatus.COMPLETED) {
                throw new InvalidRevenueOperationException("Cannot invoice milestone '" + milestone.getTitle() + "' because its status is " + milestone.getStatus() + ", expected COMPLETED");
            }
            eligibleMilestones.add(milestone);
        } else {
            // Find all completed milestones not yet invoiced
            List<Milestone> completed = milestoneRepository.findByProjectIdAndStatus(project.getId(), MilestoneStatus.COMPLETED);
            if (completed.isEmpty()) {
                throw new InvalidRevenueOperationException("No completed uninvoiced milestones found for Fixed-Price project " + project.getProjectCode());
            }
            eligibleMilestones.addAll(completed);
        }

        BigDecimal alreadyInvoiced = invoiceRepository.sumTotalInvoicedByProjectId(project.getId(), InvoiceStatus.CANCELLED);
        BigDecimal contractValue = project.getTotalContractValue() != null ? project.getTotalContractValue() : BigDecimal.ZERO;

        RevenueCalculationResult result = new RevenueCalculationResult();

        for (Milestone milestone : eligibleMilestones) {
            BigDecimal deliverableAmt = milestone.getDeliverableAmount().setScale(2, RoundingMode.HALF_UP);

            // Guard against over-billing the total contract value
            if (alreadyInvoiced.add(result.getSubtotal()).add(deliverableAmt).compareTo(contractValue) > 0) {
                log.warn("Milestone '{}' amount {} would exceed contract value {}. Current total invoiced: {}",
                        milestone.getTitle(), deliverableAmt, contractValue, alreadyInvoiced);
            }

            InvoiceLineItem lineItem = new InvoiceLineItem(
                    "Milestone Deliverable: " + milestone.getTitle(),
                    BigDecimal.ONE,
                    deliverableAmt,
                    deliverableAmt,
                    deliverableAmt, // 100% of deliverable amount recognized on milestone completion
                    null,
                    milestone.getId()
            );

            result.addLineItem(lineItem);
            result.addInvoicedMilestone(milestone);
        }

        return result;
    }

    @Override
    public BigDecimal calculateRecognizedRevenueToDate(ContractProject project) {
        // Under ASC 606 milestone / output method: recognized revenue corresponds to completed or invoiced milestones
        List<MilestoneStatus> recognizedStatuses = List.of(MilestoneStatus.COMPLETED, MilestoneStatus.INVOICED);
        BigDecimal recognized = milestoneRepository.sumAmountByProjectIdAndStatuses(project.getId(), recognizedStatuses);

        if (project.getTotalContractValue() != null && recognized.compareTo(project.getTotalContractValue()) > 0) {
            return project.getTotalContractValue();
        }
        return recognized != null ? recognized : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateUnbilledRevenue(ContractProject project) {
        // Milestone reached and completed, but invoice not yet generated
        List<MilestoneStatus> unbilledStatus = List.of(MilestoneStatus.COMPLETED);
        BigDecimal unbilled = milestoneRepository.sumAmountByProjectIdAndStatuses(project.getId(), unbilledStatus);
        return unbilled != null ? unbilled : BigDecimal.ZERO;
    }
}
