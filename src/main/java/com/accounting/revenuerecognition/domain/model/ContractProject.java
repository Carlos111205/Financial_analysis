package com.accounting.revenuerecognition.domain.model;

import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.enums.ProjectStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contract_projects", indexes = {
    @Index(name = "idx_project_code", columnList = "project_code", unique = true),
    @Index(name = "idx_pricing_model", columnList = "pricing_model"),
    @Index(name = "idx_status", columnList = "status")
})
public class ContractProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_code", nullable = false, unique = true, length = 64)
    private String projectCode;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "client_name", nullable = false, length = 255)
    private String clientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model", nullable = false, length = 32)
    private PricingModel pricingModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ProjectStatus status;

    /**
     * Applicable primarily for FIXED_PRICE contracts.
     */
    @Column(name = "total_contract_value", precision = 15, scale = 2)
    private BigDecimal totalContractValue;

    /**
     * Default hourly billing rate for TIME_AND_MATERIALS contracts.
     */
    @Column(name = "default_hourly_rate", precision = 10, scale = 2)
    private BigDecimal defaultHourlyRate;

    /**
     * Markup percentage applied for COST_PLUS contracts (e.g. 15.00 for 15%).
     */
    @Column(name = "cost_markup_percentage", precision = 6, scale = 2)
    private BigDecimal costMarkupPercentage;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityLog> activityLogs = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Invoice> invoices = new ArrayList<>();

    public ContractProject() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = ProjectStatus.PLANNED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addMilestone(Milestone milestone) {
        milestones.add(milestone);
        milestone.setProject(this);
    }

    public void removeMilestone(Milestone milestone) {
        milestones.remove(milestone);
        milestone.setProject(null);
    }

    public void addActivityLog(ActivityLog activityLog) {
        activityLogs.add(activityLog);
        activityLog.setProject(this);
    }

    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
        invoice.setProject(this);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public PricingModel getPricingModel() {
        return pricingModel;
    }

    public void setPricingModel(PricingModel pricingModel) {
        this.pricingModel = pricingModel;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalContractValue() {
        return totalContractValue;
    }

    public void setTotalContractValue(BigDecimal totalContractValue) {
        this.totalContractValue = totalContractValue;
    }

    public BigDecimal getDefaultHourlyRate() {
        return defaultHourlyRate;
    }

    public void setDefaultHourlyRate(BigDecimal defaultHourlyRate) {
        this.defaultHourlyRate = defaultHourlyRate;
    }

    public BigDecimal getCostMarkupPercentage() {
        return costMarkupPercentage;
    }

    public void setCostMarkupPercentage(BigDecimal costMarkupPercentage) {
        this.costMarkupPercentage = costMarkupPercentage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }

    public List<ActivityLog> getActivityLogs() {
        return activityLogs;
    }

    public void setActivityLogs(List<ActivityLog> activityLogs) {
        this.activityLogs = activityLogs;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }
}
