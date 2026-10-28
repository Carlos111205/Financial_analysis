package com.accounting.revenuerecognition.web;

import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.enums.ProjectStatus;
import com.accounting.revenuerecognition.dto.request.CreateMilestoneRequest;
import com.accounting.revenuerecognition.dto.request.CreateProjectRequest;
import com.accounting.revenuerecognition.dto.request.UpdateMilestoneStatusRequest;
import com.accounting.revenuerecognition.dto.request.UpdateProjectStatusRequest;
import com.accounting.revenuerecognition.dto.response.MilestoneResponse;
import com.accounting.revenuerecognition.dto.response.ProjectResponse;
import com.accounting.revenuerecognition.service.ContractProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects & Contracts", description = "Endpoints for managing contracts, pricing structures, and milestones")
public class ProjectController {

    private final ContractProjectService projectService;

    public ProjectController(ContractProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(summary = "Create a new contract project", description = "Initializes a project with a defined pricing model (FIXED_PRICE, TIME_AND_MATERIALS, COST_PLUS)")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all contracts/projects", description = "Retrieves all projects with optional filtering by pricing model or status")
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) PricingModel pricingModel,
            @RequestParam(required = false) ProjectStatus status) {
        return ResponseEntity.ok(projectService.getAllProjects(pricingModel, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by ID", description = "Fetches a project including milestone breakdown")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update project lifecycle status", description = "Transitions project status (e.g. ACTIVE, COMPLETED, ON_HOLD)")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectStatusRequest request) {
        return ResponseEntity.ok(projectService.updateProjectStatus(id, request));
    }

    @PostMapping("/{projectId}/milestones")
    @Operation(summary = "Add milestone to Fixed-Price project", description = "Creates a milestone deliverable with an allocated revenue amount")
    public ResponseEntity<MilestoneResponse> addMilestone(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateMilestoneRequest request) {
        MilestoneResponse response = projectService.addMilestone(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{projectId}/milestones")
    @Operation(summary = "Get milestones for a project", description = "Lists all defined milestones for a contract")
    public ResponseEntity<List<MilestoneResponse>> getMilestones(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getMilestones(projectId));
    }

    @PatchMapping("/{projectId}/milestones/{milestoneId}/status")
    @Operation(summary = "Update milestone status", description = "Marks a milestone as COMPLETED to trigger revenue recognition eligibility")
    public ResponseEntity<MilestoneResponse> updateMilestoneStatus(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId,
            @Valid @RequestBody UpdateMilestoneStatusRequest request) {
        return ResponseEntity.ok(projectService.updateMilestoneStatus(projectId, milestoneId, request));
    }
}
