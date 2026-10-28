package com.accounting.revenuerecognition.dto.request;

import com.accounting.revenuerecognition.domain.enums.ActivityType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LogActivityRequest {

    @NotNull(message = "Activity type is required (LABOR_HOURS, MATERIAL_EXPENSE, OTHER_DIRECT_COST)")
    private ActivityType activityType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Log date is required")
    private LocalDate logDate;

    /**
     * Applicable when activityType is LABOR_HOURS.
     */
    @DecimalMin(value = "0.01", message = "Hours worked must be greater than zero")
    private BigDecimal hoursWorked;

    /**
     * Optional billing rate override; if null, project default is used.
     */
    @DecimalMin(value = "0.00", message = "Hourly billing rate must be non-negative")
    private BigDecimal hourlyBillingRate;

    /**
     * Direct cost incurred (labor cost or baseline cost).
     */
    @NotNull(message = "Incurred cost is required")
    @DecimalMin(value = "0.00", message = "Incurred cost must be non-negative")
    private BigDecimal incurredCost;

    /**
     * Material / pass-through cost if applicable.
     */
    @DecimalMin(value = "0.00", message = "Material or direct cost must be non-negative")
    private BigDecimal materialOrDirectCost;

    public LogActivityRequest() {
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
}
