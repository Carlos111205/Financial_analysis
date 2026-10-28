package com.accounting.revenuerecognition.dto.response;

import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.enums.ProjectStatus;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectResponse {

    private Long id;
    private String projectCode;
    private String name;
    private String clientName;
    private PricingModel pricingModel;
    private ProjectStatus status;
    private BigDecimal totalContractValue;
    private BigDecimal defaultHourlyRate;
    private BigDecimal costMarkupPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
    private Instant updatedAt;
    private List<MilestoneResponse> milestones;

    public ProjectResponse() {
    }

    public static ProjectResponse fromEntity(ContractProject p) {
        ProjectResponse res = new ProjectResponse();
        res.setId(p.getId());
        res.setProjectCode(p.getProjectCode());
        res.setName(p.getName());
        res.setClientName(p.getClientName());
        res.setPricingModel(p.getPricingModel());
        res.setStatus(p.getStatus());
        res.setTotalContractValue(p.getTotalContractValue());
        res.setDefaultHourlyRate(p.getDefaultHourlyRate());
        res.setCostMarkupPercentage(p.getCostMarkupPercentage());
        res.setStartDate(p.getStartDate());
        res.setEndDate(p.getEndDate());
        res.setCreatedAt(p.getCreatedAt());
        res.setUpdatedAt(p.getUpdatedAt());

        if (p.getMilestones() != null) {
            res.setMilestones(p.getMilestones().stream()
                    .map(MilestoneResponse::fromEntity)
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

    public List<MilestoneResponse> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<MilestoneResponse> milestones) {
        this.milestones = milestones;
    }
}
