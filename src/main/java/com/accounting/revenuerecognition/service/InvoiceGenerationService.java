package com.accounting.revenuerecognition.service;

import com.accounting.revenuerecognition.domain.enums.InvoiceStatus;
import com.accounting.revenuerecognition.domain.enums.MilestoneStatus;
import com.accounting.revenuerecognition.domain.model.ActivityLog;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.domain.model.Invoice;
import com.accounting.revenuerecognition.domain.model.Milestone;
import com.accounting.revenuerecognition.dto.request.GenerateInvoiceRequest;
import com.accounting.revenuerecognition.dto.response.InvoiceResponse;
import com.accounting.revenuerecognition.exception.InvalidRevenueOperationException;
import com.accounting.revenuerecognition.exception.ResourceNotFoundException;
import com.accounting.revenuerecognition.repository.ActivityLogRepository;
import com.accounting.revenuerecognition.repository.ContractProjectRepository;
import com.accounting.revenuerecognition.repository.InvoiceRepository;
import com.accounting.revenuerecognition.repository.MilestoneRepository;
import com.accounting.revenuerecognition.service.strategy.RevenueCalculationResult;
import com.accounting.revenuerecognition.service.strategy.RevenueRecognitionStrategy;
import com.accounting.revenuerecognition.service.strategy.RevenueStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceGenerationService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceGenerationService.class);

    private final ContractProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;
    private final ActivityLogRepository activityLogRepository;
    private final MilestoneRepository milestoneRepository;
    private final RevenueStrategyFactory strategyFactory;

    public InvoiceGenerationService(ContractProjectRepository projectRepository,
                                    InvoiceRepository invoiceRepository,
                                    ActivityLogRepository activityLogRepository,
                                    MilestoneRepository milestoneRepository,
                                    RevenueStrategyFactory strategyFactory) {
        this.projectRepository = projectRepository;
        this.invoiceRepository = invoiceRepository;
        this.activityLogRepository = activityLogRepository;
        this.milestoneRepository = milestoneRepository;
        this.strategyFactory = strategyFactory;
    }

    public InvoiceResponse generateInvoice(GenerateInvoiceRequest request) {
        ContractProject project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract Project not found with ID: " + request.getProjectId()));

        // Resolve pricing model revenue strategy
        RevenueRecognitionStrategy strategy = strategyFactory.getStrategy(project.getPricingModel());
        RevenueCalculationResult calcResult = strategy.calculateRevenueAndInvoice(project, request);

        if (calcResult.getLineItems().isEmpty()) {
            throw new InvalidRevenueOperationException("No billable items or deliverable milestones resolved for invoice generation");
        }

        LocalDate issueDate = request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now();
        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : issueDate.plusDays(30);

        BigDecimal subtotal = calcResult.getSubtotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxRate = request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO;
        BigDecimal taxAmount = subtotal.multiply(taxRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        Invoice invoice = new Invoice();
        invoice.setProject(project);
        invoice.setInvoiceNumber(generateUniqueInvoiceNumber(issueDate));
        invoice.setIssueDate(issueDate);
        invoice.setDueDate(dueDate);
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setPricingModel(project.getPricingModel());
        invoice.setSubtotal(subtotal);
        invoice.setTaxRate(taxRate);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setRecognizedRevenueAmount(calcResult.getRecognizedRevenueAmount().setScale(2, RoundingMode.HALF_UP));
        invoice.setNotes(request.getNotes());

        calcResult.getLineItems().forEach(invoice::addLineItem);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Update state of billable activity logs
        for (ActivityLog activityLog : calcResult.getInvoicedActivities()) {
            activityLog.setInvoiced(true);
            activityLog.setInvoice(savedInvoice);
            activityLogRepository.save(activityLog);
        }

        // Update state of deliverable milestones
        for (Milestone milestone : calcResult.getInvoicedMilestones()) {
            milestone.setStatus(MilestoneStatus.INVOICED);
            milestoneRepository.save(milestone);
        }

        project.addInvoice(savedInvoice);
        log.info("Generated invoice {} for project {} totaling ${} (Recognized Revenue: ${})",
                savedInvoice.getInvoiceNumber(), project.getProjectCode(), savedInvoice.getTotalAmount(), savedInvoice.getRecognizedRevenueAmount());

        return InvoiceResponse.fromEntity(savedInvoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));
        return InvoiceResponse.fromEntity(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Contract Project not found with ID: " + projectId);
        }
        return invoiceRepository.findByProjectId(projectId).stream()
                .map(InvoiceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public InvoiceResponse updateInvoiceStatus(Long id, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));

        invoice.setStatus(status);
        Invoice updated = invoiceRepository.save(invoice);
        log.info("Updated invoice {} status to: {}", invoice.getInvoiceNumber(), status);
        return InvoiceResponse.fromEntity(updated);
    }

    private String generateUniqueInvoiceNumber(LocalDate date) {
        String prefix = "INV-" + date.format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + randomSuffix;
    }
}
