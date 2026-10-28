package com.accounting.revenuerecognition.service;

import com.accounting.revenuerecognition.domain.enums.InvoiceStatus;
import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.enums.ProjectStatus;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.dto.response.ProjectFinancialAnalysis;
import com.accounting.revenuerecognition.repository.ActivityLogRepository;
import com.accounting.revenuerecognition.repository.ContractProjectRepository;
import com.accounting.revenuerecognition.repository.InvoiceRepository;
import com.accounting.revenuerecognition.service.strategy.RevenueRecognitionStrategy;
import com.accounting.revenuerecognition.service.strategy.RevenueStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialAnalysisServiceTest {

    @Mock
    private ContractProjectRepository projectRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private RevenueRecognitionStrategy mockStrategy;

    private FinancialAnalysisService analysisService;
    private ContractProject project;

    @BeforeEach
    void setUp() {
        when(mockStrategy.getSupportedModel()).thenReturn(PricingModel.FIXED_PRICE);
        RevenueStrategyFactory strategyFactory = new RevenueStrategyFactory(java.util.List.of(mockStrategy));
        analysisService = new FinancialAnalysisService(projectRepository, activityLogRepository, invoiceRepository, strategyFactory);

        project = new ContractProject();
        project.setId(10L);
        project.setProjectCode("PRJ-ANALYSIS-01");
        project.setName("FinTech Core Banking");
        project.setClientName("Global Bank Ltd");
        project.setPricingModel(PricingModel.FIXED_PRICE);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setTotalContractValue(BigDecimal.valueOf(200000.00));
    }

    @Test
    void shouldAnalyzeProjectWithProfitAndUnbilledRevenue() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        // Scenario:
        // Total Incurred Cost = $60,000.00
        // Total Invoiced Amount = $80,000.00
        // Total Recognized Revenue = $100,000.00 (50% progress)
        // Expected Gross Profit = $100,000 - $60,000 = $40,000.00
        // Gross Margin % = (40,000 / 100,000) * 100 = 40.00%
        // Unbilled WIP = $100,000 - $80,000 = $20,000.00
        // Deferred Revenue = $0.00
        when(activityLogRepository.sumTotalIncurredCostByProjectId(10L)).thenReturn(BigDecimal.valueOf(60000.00));
        when(invoiceRepository.sumTotalInvoicedByProjectId(10L, InvoiceStatus.CANCELLED)).thenReturn(BigDecimal.valueOf(80000.00));
        when(mockStrategy.calculateRecognizedRevenueToDate(project)).thenReturn(BigDecimal.valueOf(100000.00));

        ProjectFinancialAnalysis analysis = analysisService.analyzeProject(10L);

        assertThat(analysis.getGrossProfit()).isEqualByComparingTo("40000.00");
        assertThat(analysis.getGrossProfitMarginPercentage()).isEqualByComparingTo("40.00");
        assertThat(analysis.getUnbilledRevenue()).isEqualByComparingTo("20000.00");
        assertThat(analysis.getDeferredRevenue()).isEqualByComparingTo("0.00");
        assertThat(analysis.getContractConsumptionPercentage()).isEqualByComparingTo("50.00");
        assertThat(analysis.getFinancialHealth()).isEqualTo("HEALTHY");
    }
}
