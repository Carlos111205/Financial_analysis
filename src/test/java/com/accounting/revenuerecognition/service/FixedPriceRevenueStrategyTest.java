package com.accounting.revenuerecognition.service;

import com.accounting.revenuerecognition.domain.enums.MilestoneStatus;
import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.domain.model.Milestone;
import com.accounting.revenuerecognition.dto.request.GenerateInvoiceRequest;
import com.accounting.revenuerecognition.exception.InvalidRevenueOperationException;
import com.accounting.revenuerecognition.repository.InvoiceRepository;
import com.accounting.revenuerecognition.repository.MilestoneRepository;
import com.accounting.revenuerecognition.service.strategy.FixedPriceRevenueStrategy;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixedPriceRevenueStrategyTest {

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private FixedPriceRevenueStrategy strategy;

    private ContractProject project;
    private Milestone milestone1;
    private Milestone milestone2;

    @BeforeEach
    void setUp() {
        project = new ContractProject();
        project.setId(1L);
        project.setProjectCode("PRJ-FIXED-01");
        project.setName("ERP Cloud Migration");
        project.setClientName("Acme Corp");
        project.setPricingModel(PricingModel.FIXED_PRICE);
        project.setTotalContractValue(BigDecimal.valueOf(100000.00));

        milestone1 = new Milestone(project, "Phase 1: Architecture Sign-off", "Deliver blueprint",
                BigDecimal.valueOf(30000.00), BigDecimal.valueOf(30.00), LocalDate.now().minusDays(10));
        milestone1.setId(10L);
        milestone1.setStatus(MilestoneStatus.COMPLETED);

        milestone2 = new Milestone(project, "Phase 2: Core Implementation", "Deliver sprint build",
                BigDecimal.valueOf(40000.00), BigDecimal.valueOf(40.00), LocalDate.now().plusDays(20));
        milestone2.setId(20L);
        milestone2.setStatus(MilestoneStatus.PENDING);
    }

    @Test
    void shouldRecognizeRevenueForCompletedMilestones() {
        when(milestoneRepository.findByProjectIdAndStatus(1L, MilestoneStatus.COMPLETED))
                .thenReturn(List.of(milestone1));
        when(invoiceRepository.sumTotalInvoicedByProjectId(eq(1L), any()))
                .thenReturn(BigDecimal.ZERO);

        GenerateInvoiceRequest request = new GenerateInvoiceRequest();
        request.setProjectId(1L);

        RevenueCalculationResult result = strategy.calculateRevenueAndInvoice(project, request);

        assertThat(result.getLineItems()).hasSize(1);
        assertThat(result.getSubtotal()).isEqualByComparingTo("30000.00");
        assertThat(result.getRecognizedRevenueAmount()).isEqualByComparingTo("30000.00");
        assertThat(result.getInvoicedMilestones()).contains(milestone1);
    }

    @Test
    void shouldThrowWhenNoCompletedMilestonesFound() {
        when(milestoneRepository.findByProjectIdAndStatus(1L, MilestoneStatus.COMPLETED))
                .thenReturn(List.of());

        GenerateInvoiceRequest request = new GenerateInvoiceRequest();
        request.setProjectId(1L);

        assertThrows(InvalidRevenueOperationException.class, () ->
                strategy.calculateRevenueAndInvoice(project, request));
    }
}
