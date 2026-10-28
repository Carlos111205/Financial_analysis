package com.accounting.revenuerecognition.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_line_items")
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * The portion of this line item recognized as earned revenue.
     */
    @Column(name = "recognized_revenue_portion", nullable = false, precision = 15, scale = 2)
    private BigDecimal recognizedRevenuePortion;

    @Column(name = "source_activity_id")
    private Long sourceActivityId;

    @Column(name = "source_milestone_id")
    private Long sourceMilestoneId;

    public InvoiceLineItem() {
    }

    public InvoiceLineItem(String description, BigDecimal quantity, BigDecimal unitPrice,
                           BigDecimal amount, BigDecimal recognizedRevenuePortion,
                           Long sourceActivityId, Long sourceMilestoneId) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.recognizedRevenuePortion = recognizedRevenuePortion != null ? recognizedRevenuePortion : amount;
        this.sourceActivityId = sourceActivityId;
        this.sourceMilestoneId = sourceMilestoneId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
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
