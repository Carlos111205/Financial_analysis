package com.accounting.revenuerecognition.dto.request;

import com.accounting.revenuerecognition.domain.enums.PricingModel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateProjectRequest {

    @NotBlank(message = "Project code is required")
    private String projectCode;

    @NotBlank(message = "Project name is required")
    private String name;

    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotNull(message = "Pricing model is required (FIXED_PRICE, TIME_AND_MATERIALS, COST_PLUS)")
    private PricingModel pricingModel;

    @DecimalMin(value = "0.00", message = "Total contract value must be non-negative")
    private BigDecimal totalContractValue;

    @DecimalMin(value = "0.00", message = "Default hourly rate must be non-negative")
    private BigDecimal defaultHourlyRate;

    @DecimalMin(value = "0.00", message = "Cost markup percentage must be non-negative")
    private BigDecimal costMarkupPercentage;

    private LocalDate startDate;
    private LocalDate endDate;

    public CreateProjectRequest() {
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
}
