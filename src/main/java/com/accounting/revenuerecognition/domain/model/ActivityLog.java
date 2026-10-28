package com.accounting.revenuerecognition.domain.model;

import com.accounting.revenuerecognition.domain.enums.ActivityType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contract_activity_logs", indexes = {
    @Index(name = "idx_activity_project", columnList = "project_id"),
    @Index(name = "idx_activity_type", columnList = "activity_type"),
    @Index(name = "idx_activity_invoiced", columnList = "is_invoiced")
})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ContractProject project;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 32)
    private ActivityType activityType;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    /**
     * Billable or logged hours (for LABOR_HOURS).
     */
    @Column(name = "hours_worked", precision = 8, scale = 2)
    private BigDecimal hoursWorked;

    /**
     * Contractual billing rate per hour (for TIME_AND_MATERIALS).
     */
    @Column(name = "hourly_billing_rate", precision = 10, scale = 2)
    private BigDecimal hourlyBillingRate;

    /**
     * Internal direct cost incurred (e.g. employee internal wage cost or base material purchase cost).
     */
    @Column(name = "incurred_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal incurredCost;

    /**
     * Material or pass-through cost (for MATERIAL_EXPENSE or OTHER_DIRECT_COST).
     */
    @Column(name = "material_or_direct_cost", precision = 15, scale = 2)
    private BigDecimal materialOrDirectCost;

    @Column(name = "is_invoiced", nullable = false)
    private boolean invoiced = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    public ActivityLog() {
    }

    public ActivityLog(ContractProject project, ActivityType activityType, String description,
                       LocalDate logDate, BigDecimal hoursWorked, BigDecimal hourlyBillingRate,
                       BigDecimal incurredCost, BigDecimal materialOrDirectCost) {
        this.project = project;
        this.activityType = activityType;
        this.description = description;
        this.logDate = logDate;
        this.hoursWorked = hoursWorked;
        this.hourlyBillingRate = hourlyBillingRate;
        this.incurredCost = incurredCost != null ? incurredCost : BigDecimal.ZERO;
        this.materialOrDirectCost = materialOrDirectCost;
        this.invoiced = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContractProject getProject() {
        return project;
    }

    public void setProject(ContractProject project) {
        this.project = project;
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
