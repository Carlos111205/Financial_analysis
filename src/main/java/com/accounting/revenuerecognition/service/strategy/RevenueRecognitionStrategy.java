package com.accounting.revenuerecognition.service.strategy;

import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.dto.request.GenerateInvoiceRequest;

import java.math.BigDecimal;

/**
 * Strategy interface defining revenue recognition calculations and automated invoicing
 * per specific contractual pricing models.
 */
public interface RevenueRecognitionStrategy {

    /**
     * The pricing model this strategy handles.
     */
    PricingModel getSupportedModel();

    /**
     * Generates invoice line items and calculates recognized revenue for a billing cycle.
     */
    RevenueCalculationResult calculateRevenueAndInvoice(ContractProject project, GenerateInvoiceRequest request);

    /**
     * Calculates cumulative recognized revenue earned to date under ASC 606 / IFRS 15.
     */
    BigDecimal calculateRecognizedRevenueToDate(ContractProject project);

    /**
     * Calculates unbilled revenue (earned Work-In-Progress) awaiting invoicing.
     */
    BigDecimal calculateUnbilledRevenue(ContractProject project);
}
