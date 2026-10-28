package com.accounting.revenuerecognition.web;

import com.accounting.revenuerecognition.dto.response.FinancialSummaryReport;
import com.accounting.revenuerecognition.dto.response.ProjectFinancialAnalysis;
import com.accounting.revenuerecognition.service.FinancialAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@Tag(name = "Financial Analysis & Reporting", description = "Endpoints for project profitability, revenue recognition schedules, and portfolio metrics")
public class FinancialAnalysisController {

    private final FinancialAnalysisService analysisService;

    public FinancialAnalysisController(FinancialAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/projects/{projectId}")
    @Operation(summary = "Project financial analysis", description = "Calculates recognized revenue, incurred cost, gross margin %, unbilled WIP, and deferred revenue for a contract")
    public ResponseEntity<ProjectFinancialAnalysis> getProjectAnalysis(@PathVariable Long projectId) {
        return ResponseEntity.ok(analysisService.analyzeProject(projectId));
    }

    @GetMapping("/summary")
    @Operation(summary = "Portfolio executive financial summary", description = "Aggregates revenue recognition, profitability, and WIP metrics across all active contracts")
    public ResponseEntity<FinancialSummaryReport> getPortfolioSummary() {
        return ResponseEntity.ok(analysisService.generateExecutiveSummary());
    }
}
