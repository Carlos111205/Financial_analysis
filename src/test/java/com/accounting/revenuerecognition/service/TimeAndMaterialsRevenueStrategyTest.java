package com.accounting.revenuerecognition.service;

import com.accounting.revenuerecognition.domain.enums.ActivityType;
import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.model.ActivityLog;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.dto.request.GenerateInvoiceRequest;
import com.accounting.revenuerecognition.repository.ActivityLogRepository;
import com.accounting.revenuerecognition.repository.InvoiceRepository;
import com.accounting.revenuerecognition.service.strategy.RevenueCalculationResult;
import com.accounting.revenuerecognition.service.strategy.TimeAndMaterialsRevenueStrategy;
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
class TimeAndMaterialsRevenueStrategyTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private TimeAndMaterialsRevenueStrategy strategy;

    private ContractProject project;
    private ActivityLog laborLog;
    private ActivityLog expenseLog;

    @BeforeEach
    void setUp() {
        project = new ContractProject();
        project.setId(2L);
        project.setProjectCode("PRJ-TM-01");
        project.setName("Mobile Banking App");
        project.setPricingModel(PricingModel.TIME_AND_MATERIALS);
        project.setDefaultHourlyRate(BigDecimal.valueOf(150.00));
        project.setCostMarkupPercentage(BigDecimal.valueOf(10.00)); // 10% on materials

        laborLog = new ActivityLog(project, ActivityType.LABOR_HOURS, "Senior Backend Dev",
                LocalDate.now().minusDays(3), BigDecimal.valueOf(40.00), BigDecimal.valueOf(150.00),
                BigDecimal.valueOf(2400.00), null);
        laborLog.setId(101L);

        expenseLog = new ActivityLog(project, ActivityType.MATERIAL_EXPENSE, "Cloud Server License",
                LocalDate.now().minusDays(2), null, null,
                BigDecimal.valueOf(500.00), BigDecimal.valueOf(500.00));
        expenseLog.setId(102L);
    }

    @Test
    void shouldCalculateCorrectInvoiceAndRevenueForLaborAndMaterials() {
        when(activityLogRepository.findUninvoicedByProjectIdAndDateBefore(eq(2L), any()))
                .thenReturn(List.of(laborLog, expenseLog));

        GenerateInvoiceRequest request = new GenerateInvoiceRequest();
        request.setProjectId(2L);

        RevenueCalculationResult result = strategy.calculateRevenueAndInvoice(project, request);

        // Labor: 40 hrs * 150 = $6,000.00
        // Material: $500 + 10% markup = $550.00
        // Expected total = $6,550.00
        assertThat(result.getLineItems()).hasSize(2);
        assertThat(result.getSubtotal()).isEqualByComparingTo("6550.00");
        assertThat(result.getRecognizedRevenueAmount()).isEqualByComparingTo("6550.00");
        assertThat(result.getInvoicedActivities()).contains(laborLog, expenseLog);
    }
}
