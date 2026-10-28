package com.accounting.revenuerecognition.web;

import com.accounting.revenuerecognition.dto.request.LogActivityRequest;
import com.accounting.revenuerecognition.dto.response.ActivityLogResponse;
import com.accounting.revenuerecognition.service.ActivityLoggingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/activities")
@Tag(name = "Project Activities & Cost Logging", description = "Endpoints to record billable labor hours, incurred costs, and material expenses")
public class ActivityController {

    private final ActivityLoggingService activityService;

    public ActivityController(ActivityLoggingService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    @Operation(summary = "Log an activity or expense", description = "Records labor hours, material costs, or direct expenses for revenue calculation")
    public ResponseEntity<ActivityLogResponse> logActivity(
            @PathVariable Long projectId,
            @Valid @RequestBody LogActivityRequest request) {
        ActivityLogResponse response = activityService.logActivity(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Retrieve activity logs for a project", description = "Fetches activity records, with optional filtering for unbilled entries only")
    public ResponseEntity<List<ActivityLogResponse>> getActivities(
            @PathVariable Long projectId,
            @RequestParam(required = false, defaultValue = "false") Boolean unbilledOnly) {
        return ResponseEntity.ok(activityService.getActivities(projectId, unbilledOnly));
    }
}
