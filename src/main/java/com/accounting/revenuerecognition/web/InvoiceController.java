package com.accounting.revenuerecognition.web;

import com.accounting.revenuerecognition.domain.enums.InvoiceStatus;
import com.accounting.revenuerecognition.dto.request.GenerateInvoiceRequest;
import com.accounting.revenuerecognition.dto.response.InvoiceResponse;
import com.accounting.revenuerecognition.service.InvoiceGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoicing & Revenue Recognition Engine", description = "Endpoints to generate automated invoices and track recognized revenue")
public class InvoiceController {

    private final InvoiceGenerationService invoiceService;

    public InvoiceController(InvoiceGenerationService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate automated invoice", description = "Calculates billing amounts and recognized revenue according to the project's pricing model (Fixed-Price, T&M, or Cost-Plus)")
    public ResponseEntity<InvoiceResponse> generateInvoice(@Valid @RequestBody GenerateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.generateInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID", description = "Fetches invoice details including line items, tax, and recognized revenue")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get all invoices for a project", description = "Lists all generated invoices associated with a specific project")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByProject(projectId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update invoice status", description = "Transitions invoice status (DRAFT, ISSUED, PAID, CANCELLED)")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam InvoiceStatus status) {
        return ResponseEntity.ok(invoiceService.updateInvoiceStatus(id, status));
    }
}
