package com.accounting.revenuerecognition.service;

import com.accounting.revenuerecognition.domain.enums.ActivityType;
import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.model.ActivityLog;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.dto.request.GenerateInvoiceRequest;
import com.accounting.revenuerecognition.repository.ActivityLogRepository;
import com.accounting.revenuerecognition.repository.InvoiceRepository;
import com.accounting.revenuerecognition.service.strategy.CostPlusRevenueStrategy;
import com.accounting.revenuerecognition.service.strategy.RevenueCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostPlusRevenueStrategyTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private CostPlusRevenueStrategy strategy;

    private ContractProject project;
    private ActivityLog costLog1;
    private ActivityLog costLog2;

    @BeforeEach
    void setUp() {
        project = new ContractProject();
        project.setId(3L);
        project.setProjectCode("PRJ-COSTPLUS-01");
        project.setName("Government Infrastructure System");
        project.setPricingModel(PricingModel.COST_PLUS);
        project.setCostMarkupPercentage(BigDecimal.valueOf(20.00)); // Cost + 20%

        costLog1 = new ActivityLog(project, ActivityType.LABOR_HOURS, "Direct Engineering Labor",
                LocalDate.now().minusDays(5), BigDecimal.valueOf(80.00), null,
                BigDecimal.valueOf(8000.00), null);
        costLog1.setId(201L);

        costLog2 = new ActivityLog(project, ActivityType.MATERIAL_EXPENSE, "Specialized Hardware Equipment",
                LocalDate.now().minusDays(3), null, null,
                BigDecimal.valueOf(2000.00), BigDecimal.valueOf(2000.00));
        costLog2.setId(202L);
    }

    @Test
    void shouldApplyMarkupPercentageToIncurredCosts() {
        when(activityLogRepository.findUninvoicedByProjectIdAndDateBefore(eq(3L), any()))
                .thenReturn(List.of(costLog1, costLog2));

        GenerateInvoiceRequest request = new GenerateInvoiceRequest();
        request.setProjectId(3L);

        RevenueCalculationResult result = strategy.calculateRevenueAndInvoice(project, request);

        // Cost 1: $8,000 * 1.20 = $9,600.00
        // Cost 2: $2,000 * 1.20 = $2,400.00
        // Total billing & recognized revenue: $12,000.00
        assertThat(result.getLineItems()).hasSize(2);
        assertThat(result.getSubtotal()).isEqualByComparingTo("12000.00");
        assertThat(result.getRecognizedRevenueAmount()).isEqualByComparingTo("12000.00");
    }
}
