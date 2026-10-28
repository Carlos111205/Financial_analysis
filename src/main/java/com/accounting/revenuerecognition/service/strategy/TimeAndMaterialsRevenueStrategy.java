package com.accounting.revenuerecognition.service.strategy;

import com.accounting.revenuerecognition.domain.enums.ActivityType;
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
 * Revenue Recognition for Time-and-Materials (T&M) Contracts:
 * Recognizes revenue directly corresponding to the right-to-invoice value of labor hours
 * worked at contractual rates, plus materials and pass-through costs.
 */
@Component
public class TimeAndMaterialsRevenueStrategy implements RevenueRecognitionStrategy {

    private final ActivityLogRepository activityLogRepository;
    private final InvoiceRepository invoiceRepository;

    public TimeAndMaterialsRevenueStrategy(ActivityLogRepository activityLogRepository,
                                           InvoiceRepository invoiceRepository) {
        this.activityLogRepository = activityLogRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public PricingModel getSupportedModel() {
        return PricingModel.TIME_AND_MATERIALS;
    }

    @Override
    public RevenueCalculationResult calculateRevenueAndInvoice(ContractProject project, GenerateInvoiceRequest request) {
        LocalDate cutoff = request.getCutoffDate() != null
                ? request.getCutoffDate()
                : (request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());

        List<ActivityLog> uninvoicedLogs = activityLogRepository.findUninvoicedByProjectIdAndDateBefore(project.getId(), cutoff);

        if (uninvoicedLogs.isEmpty()) {
            throw new InvalidRevenueOperationException("No unbilled activity logs found for T&M project " + project.getProjectCode() + " on or before " + cutoff);
        }

        RevenueCalculationResult result = new RevenueCalculationResult();
        BigDecimal markupPercentage = project.getCostMarkupPercentage() != null ? project.getCostMarkupPercentage() : BigDecimal.ZERO;
        BigDecimal markupMultiplier = BigDecimal.ONE.add(markupPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

        for (ActivityLog log : uninvoicedLogs) {
            String desc;
            BigDecimal qty;
            BigDecimal unitPrice;
            BigDecimal amount;

            if (log.getActivityType() == ActivityType.LABOR_HOURS) {
                qty = log.getHoursWorked() != null ? log.getHoursWorked() : BigDecimal.ZERO;
                unitPrice = log.getHourlyBillingRate() != null
                        ? log.getHourlyBillingRate()
                        : (project.getDefaultHourlyRate() != null ? project.getDefaultHourlyRate() : BigDecimal.ZERO);

                if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new InvalidRevenueOperationException("Missing hourly billing rate for labor activity log ID " + log.getId());
                }

                amount = qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
                desc = "Labor: " + log.getDescription() + " (" + qty + " hrs @ $" + unitPrice + "/hr)";
            } else {
                // MATERIAL_EXPENSE or OTHER_DIRECT_COST
                qty = BigDecimal.ONE;
                BigDecimal baseCost = log.getMaterialOrDirectCost() != null ? log.getMaterialOrDirectCost() : log.getIncurredCost();
                unitPrice = baseCost.multiply(markupMultiplier).setScale(2, RoundingMode.HALF_UP);
                amount = unitPrice;
                desc = log.getActivityType() + ": " + log.getDescription() + (markupPercentage.compareTo(BigDecimal.ZERO) > 0 ? " (incl. " + markupPercentage + "% markup)" : "");
            }

            InvoiceLineItem item = new InvoiceLineItem(
                    desc,
                    qty,
                    unitPrice,
                    amount,
                    amount, // In T&M, recognized revenue matches billable amount as services are delivered
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
        // Recognized from issued invoices + unbilled WIP
        BigDecimal recognizedFromInvoices = invoiceRepository.sumRecognizedRevenueByProjectId(project.getId(), InvoiceStatus.CANCELLED);
        BigDecimal unbilledWip = calculateUnbilledRevenue(project);
        return recognizedFromInvoices.add(unbilledWip);
    }

    @Override
    public BigDecimal calculateUnbilledRevenue(ContractProject project) {
        List<ActivityLog> unbilled = activityLogRepository.findByProjectIdAndInvoicedFalse(project.getId());
        BigDecimal totalUnbilled = BigDecimal.ZERO;

        BigDecimal markupPercentage = project.getCostMarkupPercentage() != null ? project.getCostMarkupPercentage() : BigDecimal.ZERO;
        BigDecimal markupMultiplier = BigDecimal.ONE.add(markupPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

        for (ActivityLog log : unbilled) {
            if (log.getActivityType() == ActivityType.LABOR_HOURS) {
                BigDecimal qty = log.getHoursWorked() != null ? log.getHoursWorked() : BigDecimal.ZERO;
                BigDecimal rate = log.getHourlyBillingRate() != null
                        ? log.getHourlyBillingRate()
                        : (project.getDefaultHourlyRate() != null ? project.getDefaultHourlyRate() : BigDecimal.ZERO);
                totalUnbilled = totalUnbilled.add(qty.multiply(rate));
            } else {
                BigDecimal baseCost = log.getMaterialOrDirectCost() != null ? log.getMaterialOrDirectCost() : log.getIncurredCost();
                if (baseCost != null) {
                    totalUnbilled = totalUnbilled.add(baseCost.multiply(markupMultiplier));
                }
            }
        }

        return totalUnbilled.setScale(2, RoundingMode.HALF_UP);
    }
}
