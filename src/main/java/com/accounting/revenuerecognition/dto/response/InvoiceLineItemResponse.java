package com.accounting.revenuerecognition.dto.response;

import com.accounting.revenuerecognition.domain.model.InvoiceLineItem;
import java.math.BigDecimal;

public class InvoiceLineItemResponse {

    private Long id;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private BigDecimal recognizedRevenuePortion;
    private Long sourceActivityId;
    private Long sourceMilestoneId;

    public InvoiceLineItemResponse() {
    }

    public static InvoiceLineItemResponse fromEntity(InvoiceLineItem item) {
        InvoiceLineItemResponse res = new InvoiceLineItemResponse();
        res.setId(item.getId());
        res.setDescription(item.getDescription());
        res.setQuantity(item.getQuantity());
        res.setUnitPrice(item.getUnitPrice());
        res.setAmount(item.getAmount());
        res.setRecognizedRevenuePortion(item.getRecognizedRevenuePortion());
        res.setSourceActivityId(item.getSourceActivityId());
        res.setSourceMilestoneId(item.getSourceMilestoneId());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRecognizedRevenuePortion() {
        return recognizedRevenuePortion;
    }

    public void setRecognizedRevenuePortion(BigDecimal recognizedRevenuePortion) {
        this.recognizedRevenuePortion = recognizedRevenuePortion;
    }

    public Long getSourceActivityId() {
        return sourceActivityId;
    }

    public void setSourceActivityId(Long sourceActivityId) {
        this.sourceActivityId = sourceActivityId;
    }

    public Long getSourceMilestoneId() {
        return sourceMilestoneId;
    }

    public void setSourceMilestoneId(Long sourceMilestoneId) {
        this.sourceMilestoneId = sourceMilestoneId;
    }
}
