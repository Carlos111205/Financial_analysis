package com.accounting.revenuerecognition.service;

import com.accounting.revenuerecognition.domain.enums.InvoiceStatus;
import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.dto.response.FinancialSummaryReport;
import com.accounting.revenuerecognition.dto.response.ProjectFinancialAnalysis;
import com.accounting.revenuerecognition.exception.ResourceNotFoundException;
import com.accounting.revenuerecognition.repository.ActivityLogRepository;
import com.accounting.revenuerecognition.repository.ContractProjectRepository;
import com.accounting.revenuerecognition.repository.InvoiceRepository;
import com.accounting.revenuerecognition.service.strategy.RevenueRecognitionStrategy;
import com.accounting.revenuerecognition.service.strategy.RevenueStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FinancialAnalysisService {

    private final ContractProjectRepository projectRepository;
    private final ActivityLogRepository activityLogRepository;
    private final InvoiceRepository invoiceRepository;
    private final RevenueStrategyFactory strategyFactory;

    public FinancialAnalysisService(ContractProjectRepository projectRepository,
                                    ActivityLogRepository activityLogRepository,
                                    InvoiceRepository invoiceRepository,
                                    RevenueStrategyFactory strategyFactory) {
        this.projectRepository = projectRepository;
        this.activityLogRepository = activityLogRepository;
        this.invoiceRepository = invoiceRepository;
        this.strategyFactory = strategyFactory;
    }

    /**
     * Computes deep financial analysis and revenue recognition metrics for a specific project.
     */
    public ProjectFinancialAnalysis analyzeProject(Long projectId) {
        ContractProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract Project not found with ID: " + projectId));

        RevenueRecognitionStrategy strategy = strategyFactory.getStrategy(project.getPricingModel());

        BigDecimal totalContractValue = project.getTotalContractValue() != null ? project.getTotalContractValue() : BigDecimal.ZERO;
        BigDecimal totalIncurredCost = activityLogRepository.sumTotalIncurredCostByProjectId(projectId);
        BigDecimal totalInvoicedAmount = invoiceRepository.sumTotalInvoicedByProjectId(projectId, InvoiceStatus.CANCELLED);
        BigDecimal totalRecognizedRevenue = strategy.calculateRecognizedRevenueToDate(project);

        BigDecimal grossProfit = totalRecognizedRevenue.subtract(totalIncurredCost).setScale(2, RoundingMode.HALF_UP);

        BigDecimal grossMarginPct = BigDecimal.ZERO;
        if (totalRecognizedRevenue.compareTo(BigDecimal.ZERO) > 0) {
            grossMarginPct = grossProfit
                    .divide(totalRecognizedRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Unbilled Revenue (WIP) vs Deferred Revenue (Unearned Revenue)
        BigDecimal unbilledRevenue = BigDecimal.ZERO;
        BigDecimal deferredRevenue = BigDecimal.ZERO;

        if (totalRecognizedRevenue.compareTo(totalInvoicedAmount) > 0) {
            unbilledRevenue = totalRecognizedRevenue.subtract(totalInvoicedAmount).setScale(2, RoundingMode.HALF_UP);
        } else if (totalInvoicedAmount.compareTo(totalRecognizedRevenue) > 0) {
            deferredRevenue = totalInvoicedAmount.subtract(totalRecognizedRevenue).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal contractConsumptionPct = BigDecimal.ZERO;
        if (totalContractValue.compareTo(BigDecimal.ZERO) > 0) {
            contractConsumptionPct = totalRecognizedRevenue
                    .divide(totalContractValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        String health;
        if (grossProfit.compareTo(BigDecimal.ZERO) < 0) {
            health = "LOSS_WARNING";
        } else if (grossMarginPct.compareTo(BigDecimal.valueOf(20.00)) >= 0) {
            health = "HEALTHY";
        } else {
            health = "MODERATE";
        }

        ProjectFinancialAnalysis analysis = new ProjectFinancialAnalysis();
        analysis.setProjectId(project.getId());
        analysis.setProjectCode(project.getProjectCode());
        analysis.setProjectName(project.getName());
        analysis.setClientName(project.getClientName());
        analysis.setPricingModel(project.getPricingModel());
        analysis.setProjectStatus(project.getStatus());
        analysis.setTotalContractValue(totalContractValue);
        analysis.setTotalIncurredCost(totalIncurredCost.setScale(2, RoundingMode.HALF_UP));
        analysis.setTotalInvoicedAmount(totalInvoicedAmount.setScale(2, RoundingMode.HALF_UP));
        analysis.setTotalRecognizedRevenue(totalRecognizedRevenue.setScale(2, RoundingMode.HALF_UP));
        analysis.setGrossProfit(grossProfit);
        analysis.setGrossProfitMarginPercentage(grossMarginPct);
        analysis.setUnbilledRevenue(unbilledRevenue);
        analysis.setDeferredRevenue(deferredRevenue);
        analysis.setContractConsumptionPercentage(contractConsumptionPct);
        analysis.setFinancialHealth(health);

        return analysis;
    }

    /**
     * Aggregates portfolio-wide revenue recognition metrics across all contract projects.
     */
    public FinancialSummaryReport generateExecutiveSummary() {
        List<ContractProject> allProjects = projectRepository.findAll();
        List<ProjectFinancialAnalysis> projectAnalyses = new ArrayList<>();

        BigDecimal totalPortfolioContractValue = BigDecimal.ZERO;
        BigDecimal totalPortfolioIncurredCost = BigDecimal.ZERO;
        BigDecimal totalPortfolioInvoicedAmount = BigDecimal.ZERO;
        BigDecimal totalPortfolioRecognizedRevenue = BigDecimal.ZERO;
        BigDecimal totalUnbilledWIP = BigDecimal.ZERO;
        BigDecimal totalDeferredRevenue = BigDecimal.ZERO;

        for (ContractProject p : allProjects) {
            ProjectFinancialAnalysis analysis = analyzeProject(p.getId());
            projectAnalyses.add(analysis);

            totalPortfolioContractValue = totalPortfolioContractValue.add(analysis.getTotalContractValue());
            totalPortfolioIncurredCost = totalPortfolioIncurredCost.add(analysis.getTotalIncurredCost());
            totalPortfolioInvoicedAmount = totalPortfolioInvoicedAmount.add(analysis.getTotalInvoicedAmount());
            totalPortfolioRecognizedRevenue = totalPortfolioRecognizedRevenue.add(analysis.getTotalRecognizedRevenue());
            totalUnbilledWIP = totalUnbilledWIP.add(analysis.getUnbilledRevenue());
            totalDeferredRevenue = totalDeferredRevenue.add(analysis.getDeferredRevenue());
        }

        BigDecimal totalGrossProfit = totalPortfolioRecognizedRevenue.subtract(totalPortfolioIncurredCost).setScale(2, RoundingMode.HALF_UP);
        BigDecimal portfolioGrossMargin = BigDecimal.ZERO;
        if (totalPortfolioRecognizedRevenue.compareTo(BigDecimal.ZERO) > 0) {
            portfolioGrossMargin = totalGrossProfit
                    .divide(totalPortfolioRecognizedRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        Map<String, Long> byModel = allProjects.stream()
                .collect(Collectors.groupingBy(p -> p.getPricingModel().name(), Collectors.counting()));

        Map<String, Long> byStatus = allProjects.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));

        FinancialSummaryReport report = new FinancialSummaryReport();
        report.setTotalProjectsCount(allProjects.size());
        report.setTotalPortfolioContractValue(totalPortfolioContractValue.setScale(2, RoundingMode.HALF_UP));
        report.setTotalPortfolioIncurredCost(totalPortfolioIncurredCost.setScale(2, RoundingMode.HALF_UP));
        report.setTotalPortfolioInvoicedAmount(totalPortfolioInvoicedAmount.setScale(2, RoundingMode.HALF_UP));
        report.setTotalPortfolioRecognizedRevenue(totalPortfolioRecognizedRevenue.setScale(2, RoundingMode.HALF_UP));
        report.setTotalPortfolioGrossProfit(totalGrossProfit);
        report.setPortfolioGrossProfitMarginPercentage(portfolioGrossMargin);
        report.setTotalUnbilledRevenueWIP(totalUnbilledWIP.setScale(2, RoundingMode.HALF_UP));
        report.setTotalDeferredRevenue(totalDeferredRevenue.setScale(2, RoundingMode.HALF_UP));
        report.setProjectsByPricingModel(byModel);
        report.setProjectsByStatus(byStatus);
        report.setProjectAnalyses(projectAnalyses);

        return report;
    }
}
