package com.accounting.revenuerecognition.dto.response;

import com.accounting.revenuerecognition.domain.enums.ActivityType;
import com.accounting.revenuerecognition.domain.model.ActivityLog;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ActivityLogResponse {

    private Long id;
    private Long projectId;
    private ActivityType activityType;
    private String description;
    private LocalDate logDate;
    private BigDecimal hoursWorked;
    private BigDecimal hourlyBillingRate;
    private BigDecimal incurredCost;
    private BigDecimal materialOrDirectCost;
    private boolean invoiced;
    private Long invoiceId;

    public ActivityLogResponse() {
    }

    public static ActivityLogResponse fromEntity(ActivityLog a) {
        ActivityLogResponse res = new ActivityLogResponse();
        res.setId(a.getId());
        res.setProjectId(a.getProject() != null ? a.getProject().getId() : null);
        res.setActivityType(a.getActivityType());
        res.setDescription(a.getDescription());
        res.setLogDate(a.getLogDate());
        res.setHoursWorked(a.getHoursWorked());
        res.setHourlyBillingRate(a.getHourlyBillingRate());
        res.setIncurredCost(a.getIncurredCost());
        res.setMaterialOrDirectCost(a.getMaterialOrDirectCost());
        res.setInvoiced(a.isInvoiced());
        res.setInvoiceId(a.getInvoice() != null ? a.getInvoice().getId() : null);
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public BigDecimal getHourlyBillingRate() {
        return hourlyBillingRate;
    }

    public void setHourlyBillingRate(BigDecimal hourlyBillingRate) {
        this.hourlyBillingRate = hourlyBillingRate;
    }

    public BigDecimal getIncurredCost() {
        return incurredCost;
    }

    public void setIncurredCost(BigDecimal incurredCost) {
        this.incurredCost = incurredCost;
    }

    public BigDecimal getMaterialOrDirectCost() {
        return materialOrDirectCost;
    }

    public void setMaterialOrDirectCost(BigDecimal materialOrDirectCost) {
        this.materialOrDirectCost = materialOrDirectCost;
    }

    public boolean isInvoiced() {
        return invoiced;
    }

    public void setInvoiced(boolean invoiced) {
        this.invoiced = invoiced;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
}
