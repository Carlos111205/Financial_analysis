package com.accounting.revenuerecognition.dto.response;

import com.accounting.revenuerecognition.domain.enums.InvoiceStatus;
import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.model.Invoice;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private Long projectId;
    private String projectCode;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private PricingModel pricingModel;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal recognizedRevenueAmount;
    private String notes;
    private Instant createdAt;
    private List<InvoiceLineItemResponse> lineItems;

    public InvoiceResponse() {
    }

    public static InvoiceResponse fromEntity(Invoice invoice) {
        InvoiceResponse res = new InvoiceResponse();
        res.setId(invoice.getId());
        res.setInvoiceNumber(invoice.getInvoiceNumber());
        if (invoice.getProject() != null) {
            res.setProjectId(invoice.getProject().getId());
            res.setProjectCode(invoice.getProject().getProjectCode());
        }
        res.setIssueDate(invoice.getIssueDate());
        res.setDueDate(invoice.getDueDate());
        res.setStatus(invoice.getStatus());
        res.setPricingModel(invoice.getPricingModel());
        res.setSubtotal(invoice.getSubtotal());
        res.setTaxRate(invoice.getTaxRate());
        res.setTaxAmount(invoice.getTaxAmount());
        res.setTotalAmount(invoice.getTotalAmount());
        res.setRecognizedRevenueAmount(invoice.getRecognizedRevenueAmount());
        res.setNotes(invoice.getNotes());
        res.setCreatedAt(invoice.getCreatedAt());

        if (invoice.getLineItems() != null) {
            res.setLineItems(invoice.getLineItems().stream()
                    .map(InvoiceLineItemResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public PricingModel getPricingModel() {
        return pricingModel;
    }

    public void setPricingModel(PricingModel pricingModel) {
        this.pricingModel = pricingModel;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRecognizedRevenueAmount() {
        return recognizedRevenueAmount;
    }

    public void setRecognizedRevenueAmount(BigDecimal recognizedRevenueAmount) {
        this.recognizedRevenueAmount = recognizedRevenueAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<InvoiceLineItemResponse> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<InvoiceLineItemResponse> lineItems) {
        this.lineItems = lineItems;
    }
}
