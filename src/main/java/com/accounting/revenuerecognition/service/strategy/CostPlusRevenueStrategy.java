package com.accounting.revenuerecognition.service.strategy;

import com.accounting.revenuerecognition.domain.enums.InvoiceStatus;
import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.model.ActivityLog;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.domain.model.InvoiceLineItem;
import com.accounting.revenuerecognition.dto.request.GenerateInvoiceRequest;
import com.accounting.revenuerecognition.exception.InvalidRevenueOperationException;
import com.accounting.revenuerecognition.repository.ActivityLogRepository;
import com.accounting.revenuerecognition.repository.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Revenue Recognition for Cost-Plus Contracts:
 * Invoicing and recognized revenue equal allowable direct incurred costs
 * plus an agreed contractual markup percentage (e.g. Cost + 15%).
 */
@Component
public class CostPlusRevenueStrategy implements RevenueRecognitionStrategy {

    private final ActivityLogRepository activityLogRepository;
    private final InvoiceRepository invoiceRepository;

    public CostPlusRevenueStrategy(ActivityLogRepository activityLogRepository,
                                   InvoiceRepository invoiceRepository) {
        this.activityLogRepository = activityLogRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public PricingModel getSupportedModel() {
        return PricingModel.COST_PLUS;
    }

    @Override
    public RevenueCalculationResult calculateRevenueAndInvoice(ContractProject project, GenerateInvoiceRequest request) {
        LocalDate cutoff = request.getCutoffDate() != null
                ? request.getCutoffDate()
                : (request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());

        List<ActivityLog> uninvoicedLogs = activityLogRepository.findUninvoicedByProjectIdAndDateBefore(project.getId(), cutoff);

        if (uninvoicedLogs.isEmpty()) {
            throw new InvalidRevenueOperationException("No unbilled cost records found for Cost-Plus project " + project.getProjectCode() + " on or before " + cutoff);
        }

        BigDecimal markupPercentage = project.getCostMarkupPercentage() != null ? project.getCostMarkupPercentage() : BigDecimal.ZERO;
        BigDecimal markupMultiplier = BigDecimal.ONE.add(markupPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

        RevenueCalculationResult result = new RevenueCalculationResult();

        for (ActivityLog log : uninvoicedLogs) {
            BigDecimal baseCost = log.getIncurredCost() != null ? log.getIncurredCost() : BigDecimal.ZERO;
            if (baseCost.compareTo(BigDecimal.ZERO) == 0 && log.getMaterialOrDirectCost() != null) {
                baseCost = log.getMaterialOrDirectCost();
            }

            BigDecimal billableAmount = baseCost.multiply(markupMultiplier).setScale(2, RoundingMode.HALF_UP);
            BigDecimal markupAmount = billableAmount.subtract(baseCost).setScale(2, RoundingMode.HALF_UP);

            String desc = "Cost-Plus [" + log.getActivityType() + "]: " + log.getDescription()
                    + " (Base Cost: $" + baseCost + " + " + markupPercentage + "% Markup: $" + markupAmount + ")";

            InvoiceLineItem item = new InvoiceLineItem(
                    desc,
                    BigDecimal.ONE,
                    billableAmount,
                    billableAmount,
                    billableAmount, // Recognized revenue comprises both cost recovery and contractual markup
                    log.getId(),
                    null
            );

            result.addLineItem(item);
            result.addInvoicedActivity(log);
        }

        return result;
    }

    @Override
    public BigDecimal calculateRecognizedRevenueToDate(ContractProject project) {
        BigDecimal recognizedFromInvoices = invoiceRepository.sumRecognizedRevenueByProjectId(project.getId(), InvoiceStatus.CANCELLED);
        BigDecimal unbilledWip = calculateUnbilledRevenue(project);
        return recognizedFromInvoices.add(unbilledWip);
    }

    @Override
    public BigDecimal calculateUnbilledRevenue(ContractProject project) {
        List<ActivityLog> unbilled = activityLogRepository.findByProjectIdAndInvoicedFalse(project.getId());
        BigDecimal totalCost = BigDecimal.ZERO;

        for (ActivityLog log : unbilled) {
            BigDecimal baseCost = log.getIncurredCost() != null ? log.getIncurredCost() : BigDecimal.ZERO;
            if (baseCost.compareTo(BigDecimal.ZERO) == 0 && log.getMaterialOrDirectCost() != null) {
                baseCost = log.getMaterialOrDirectCost();
            }
            totalCost = totalCost.add(baseCost);
        }

        BigDecimal markupPercentage = project.getCostMarkupPercentage() != null ? project.getCostMarkupPercentage() : BigDecimal.ZERO;
        BigDecimal markupMultiplier = BigDecimal.ONE.add(markupPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

        return totalCost.multiply(markupMultiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
