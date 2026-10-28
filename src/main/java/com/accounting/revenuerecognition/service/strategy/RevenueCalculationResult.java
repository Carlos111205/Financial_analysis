package com.accounting.revenuerecognition.service.strategy;

import com.accounting.revenuerecognition.domain.model.ActivityLog;
import com.accounting.revenuerecognition.domain.model.InvoiceLineItem;
import com.accounting.revenuerecognition.domain.model.Milestone;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RevenueCalculationResult {

    private final List<InvoiceLineItem> lineItems = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal recognizedRevenueAmount = BigDecimal.ZERO;
    private final List<ActivityLog> invoicedActivities = new ArrayList<>();
    private final List<Milestone> invoicedMilestones = new ArrayList<>();

    public RevenueCalculationResult() {
    }

    public void addLineItem(InvoiceLineItem item) {
        lineItems.add(item);
        this.subtotal = this.subtotal.add(item.getAmount());
        this.recognizedRevenueAmount = this.recognizedRevenueAmount.add(item.getRecognizedRevenuePortion());
    }

    public void addInvoicedActivity(ActivityLog activityLog) {
        this.invoicedActivities.add(activityLog);
    }

    public void addInvoicedMilestone(Milestone milestone) {
        this.invoicedMilestones.add(milestone);
    }

    public List<InvoiceLineItem> getLineItems() {
        return lineItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getRecognizedRevenueAmount() {
        return recognizedRevenueAmount;
    }

    public void setRecognizedRevenueAmount(BigDecimal recognizedRevenueAmount) {
        this.recognizedRevenueAmount = recognizedRevenueAmount;
    }

    public List<ActivityLog> getInvoicedActivities() {
        return invoicedActivities;
    }

    public List<Milestone> getInvoicedMilestones() {
        return invoicedMilestones;
    }
}
