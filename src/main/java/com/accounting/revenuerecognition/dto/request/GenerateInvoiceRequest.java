package com.accounting.revenuerecognition.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GenerateInvoiceRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private LocalDate issueDate;
    private LocalDate dueDate;

    /**
     * Optional cutoff date for billable activity logs. Defaults to issueDate if omitted.
     */
    private LocalDate cutoffDate;

    /**
     * Optional specific milestone ID to invoice for FIXED_PRICE contracts.
     * If omitted, all completed uninvoiced milestones will be billed.
     */
    private Long targetMilestoneId;

    @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Tax rate cannot exceed 100%")
    private BigDecimal taxRate;

    private String notes;

    public GenerateInvoiceRequest() {
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public LocalDate getCutoffDate() {
        return cutoffDate;
    }

    public void setCutoffDate(LocalDate cutoffDate) {
        this.cutoffDate = cutoffDate;
    }

    public Long getTargetMilestoneId() {
        return targetMilestoneId;
    }

    public void setTargetMilestoneId(Long targetMilestoneId) {
        this.targetMilestoneId = targetMilestoneId;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
