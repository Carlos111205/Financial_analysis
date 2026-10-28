package com.accounting.revenuerecognition.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class FinancialSummaryReport {

    private Instant generatedAt;
    private int totalProjectsCount;
    private BigDecimal totalPortfolioContractValue;
    private BigDecimal totalPortfolioIncurredCost;
    private BigDecimal totalPortfolioInvoicedAmount;
    private BigDecimal totalPortfolioRecognizedRevenue;
    private BigDecimal totalPortfolioGrossProfit;
    private BigDecimal portfolioGrossProfitMarginPercentage;
    private BigDecimal totalUnbilledRevenueWIP;
    private BigDecimal totalDeferredRevenue;

    private Map<String, Long> projectsByPricingModel;
    private Map<String, Long> projectsByStatus;
    private List<ProjectFinancialAnalysis> projectAnalyses;

    public FinancialSummaryReport() {
        this.generatedAt = Instant.now();
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public int getTotalProjectsCount() {
        return totalProjectsCount;
    }

    public void setTotalProjectsCount(int totalProjectsCount) {
        this.totalProjectsCount = totalProjectsCount;
    }

    public BigDecimal getTotalPortfolioContractValue() {
        return totalPortfolioContractValue;
    }

    public void setTotalPortfolioContractValue(BigDecimal totalPortfolioContractValue) {
        this.totalPortfolioContractValue = totalPortfolioContractValue;
    }

    public BigDecimal getTotalPortfolioIncurredCost() {
        return totalPortfolioIncurredCost;
    }

    public void setTotalPortfolioIncurredCost(BigDecimal totalPortfolioIncurredCost) {
        this.totalPortfolioIncurredCost = totalPortfolioIncurredCost;
    }

    public BigDecimal getTotalPortfolioInvoicedAmount() {
        return totalPortfolioInvoicedAmount;
    }

    public void setTotalPortfolioInvoicedAmount(BigDecimal totalPortfolioInvoicedAmount) {
        this.totalPortfolioInvoicedAmount = totalPortfolioInvoicedAmount;
    }

    public BigDecimal getTotalPortfolioRecognizedRevenue() {
        return totalPortfolioRecognizedRevenue;
    }

    public void setTotalPortfolioRecognizedRevenue(BigDecimal totalPortfolioRecognizedRevenue) {
        this.totalPortfolioRecognizedRevenue = totalPortfolioRecognizedRevenue;
    }

    public BigDecimal getTotalPortfolioGrossProfit() {
        return totalPortfolioGrossProfit;
    }

    public void setTotalPortfolioGrossProfit(BigDecimal totalPortfolioGrossProfit) {
        this.totalPortfolioGrossProfit = totalPortfolioGrossProfit;
    }

    public BigDecimal getPortfolioGrossProfitMarginPercentage() {
        return portfolioGrossProfitMarginPercentage;
    }

    public void setPortfolioGrossProfitMarginPercentage(BigDecimal portfolioGrossProfitMarginPercentage) {
        this.portfolioGrossProfitMarginPercentage = portfolioGrossProfitMarginPercentage;
    }

    public BigDecimal getTotalUnbilledRevenueWIP() {
        return totalUnbilledRevenueWIP;
    }

    public void setTotalUnbilledRevenueWIP(BigDecimal totalUnbilledRevenueWIP) {
        this.totalUnbilledRevenueWIP = totalUnbilledRevenueWIP;
    }

    public BigDecimal getTotalDeferredRevenue() {
        return totalDeferredRevenue;
    }

    public void setTotalDeferredRevenue(BigDecimal totalDeferredRevenue) {
        this.totalDeferredRevenue = totalDeferredRevenue;
    }

    public Map<String, Long> getProjectsByPricingModel() {
        return projectsByPricingModel;
    }

    public void setProjectsByPricingModel(Map<String, Long> projectsByPricingModel) {
        this.projectsByPricingModel = projectsByPricingModel;
    }

    public Map<String, Long> getProjectsByStatus() {
        return projectsByStatus;
    }

    public void setProjectsByStatus(Map<String, Long> projectsByStatus) {
        this.projectsByStatus = projectsByStatus;
    }

    public List<ProjectFinancialAnalysis> getProjectAnalyses() {
        return projectAnalyses;
    }

    public void setProjectAnalyses(List<ProjectFinancialAnalysis> projectAnalyses) {
        this.projectAnalyses = projectAnalyses;
    }
}
