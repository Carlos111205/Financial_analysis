package com.accounting.revenuerecognition.dto.response;

import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.enums.ProjectStatus;
import java.math.BigDecimal;

public class ProjectFinancialAnalysis {

    private Long projectId;
    private String projectCode;
    private String projectName;
    private String clientName;
    private PricingModel pricingModel;
    private ProjectStatus projectStatus;

    /**
     * Total contract value (for Fixed-Price or budget cap).
     */
    private BigDecimal totalContractValue;

    /**
     * Total direct costs incurred to date (labor + materials + other costs).
     */
    private BigDecimal totalIncurredCost;

    /**
     * Total amounts billed to client (excluding cancelled invoices).
     */
    private BigDecimal totalInvoicedAmount;

    /**
     * Total earned revenue recognized under ASC 606 / IFRS 15 principles.
     */
    private BigDecimal totalRecognizedRevenue;

    /**
     * Recognized Revenue minus Incurred Cost.
     */
    private BigDecimal grossProfit;

    /**
     * Gross Profit Margin percentage: (Gross Profit / Recognized Revenue) * 100.
     */
    private BigDecimal grossProfitMarginPercentage;

    /**
     * Unbilled Revenue / Work-in-Progress (WIP):
     * Earned revenue recognized that has not yet been billed (Recognized > Invoiced).
     */
    private BigDecimal unbilledRevenue;

    /**
     * Deferred Revenue / Unearned Revenue:
     * Invoiced amounts in advance of performance obligations / delivery (Invoiced > Recognized).
     */
    private BigDecimal deferredRevenue;

    /**
     * Budget or Contract Value consumption percentage: (Recognized / Contract Value) * 100.
     */
    private BigDecimal contractConsumptionPercentage;

    /**
     * Financial Health Indicator: HEALTHY, MODERATE, LOSS_WARNING.
     */
    private String financialHealth;

    public ProjectFinancialAnalysis() {
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public ProjectStatus getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    public BigDecimal getTotalContractValue() {
        return totalContractValue;
    }

    public void setTotalContractValue(BigDecimal totalContractValue) {
        this.totalContractValue = totalContractValue;
    }

    public BigDecimal getTotalIncurredCost() {
        return totalIncurredCost;
    }

    public void setTotalIncurredCost(BigDecimal totalIncurredCost) {
        this.totalIncurredCost = totalIncurredCost;
    }

    public BigDecimal getTotalInvoicedAmount() {
        return totalInvoicedAmount;
    }

    public void setTotalInvoicedAmount(BigDecimal totalInvoicedAmount) {
        this.totalInvoicedAmount = totalInvoicedAmount;
    }

    public BigDecimal getTotalRecognizedRevenue() {
        return totalRecognizedRevenue;
    }

    public void setTotalRecognizedRevenue(BigDecimal totalRecognizedRevenue) {
        this.totalRecognizedRevenue = totalRecognizedRevenue;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public BigDecimal getGrossProfitMarginPercentage() {
        return grossProfitMarginPercentage;
    }

    public void setGrossProfitMarginPercentage(BigDecimal grossProfitMarginPercentage) {
        this.grossProfitMarginPercentage = grossProfitMarginPercentage;
    }

    public BigDecimal getUnbilledRevenue() {
        return unbilledRevenue;
    }

    public void setUnbilledRevenue(BigDecimal unbilledRevenue) {
        this.unbilledRevenue = unbilledRevenue;
    }

    public BigDecimal getDeferredRevenue() {
        return deferredRevenue;
    }

    public void setDeferredRevenue(BigDecimal deferredRevenue) {
        this.deferredRevenue = deferredRevenue;
    }

    public BigDecimal getContractConsumptionPercentage() {
        return contractConsumptionPercentage;
    }

    public void setContractConsumptionPercentage(BigDecimal contractConsumptionPercentage) {
        this.contractConsumptionPercentage = contractConsumptionPercentage;
    }

    public String getFinancialHealth() {
        return financialHealth;
    }

    public void setFinancialHealth(String financialHealth) {
        this.financialHealth = financialHealth;
    }
}
