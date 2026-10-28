package com.accounting.revenuerecognition.service;

import com.accounting.revenuerecognition.domain.enums.ActivityType;
import com.accounting.revenuerecognition.domain.model.ActivityLog;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.dto.request.LogActivityRequest;
import com.accounting.revenuerecognition.dto.response.ActivityLogResponse;
import com.accounting.revenuerecognition.exception.InvalidRevenueOperationException;
import com.accounting.revenuerecognition.exception.ResourceNotFoundException;
import com.accounting.revenuerecognition.repository.ActivityLogRepository;
import com.accounting.revenuerecognition.repository.ContractProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActivityLoggingService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLoggingService.class);

    private final ActivityLogRepository activityLogRepository;
    private final ContractProjectRepository projectRepository;

    public ActivityLoggingService(ActivityLogRepository activityLogRepository,
                                  ContractProjectRepository projectRepository) {
        this.activityLogRepository = activityLogRepository;
        this.projectRepository = projectRepository;
    }

    public ActivityLogResponse logActivity(Long projectId, LogActivityRequest request) {
        ContractProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract Project not found with ID: " + projectId));

        // Model specific validations
        if (request.getActivityType() == ActivityType.LABOR_HOURS) {
            if (request.getHoursWorked() == null || request.getHoursWorked().signum() <= 0) {
                throw new InvalidRevenueOperationException("Labor hours activity requires positive hours worked");
            }
        }

        ActivityLog activityLog = new ActivityLog();
        activityLog.setProject(project);
        activityLog.setActivityType(request.getActivityType());
        activityLog.setDescription(request.getDescription());
        activityLog.setLogDate(request.getLogDate());
        activityLog.setHoursWorked(request.getHoursWorked());
        activityLog.setHourlyBillingRate(request.getHourlyBillingRate() != null
                ? request.getHourlyBillingRate()
                : project.getDefaultHourlyRate());
        activityLog.setIncurredCost(request.getIncurredCost());
        activityLog.setMaterialOrDirectCost(request.getMaterialOrDirectCost());
        activityLog.setInvoiced(false);

        project.addActivityLog(activityLog);
        ActivityLog saved = activityLogRepository.save(activityLog);
        log.info("Logged activity ID: {} for project: {}", saved.getId(), project.getProjectCode());
        return ActivityLogResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getActivities(Long projectId, Boolean unbilledOnly) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Contract Project not found with ID: " + projectId);
        }

        List<ActivityLog> logs = (Boolean.TRUE.equals(unbilledOnly))
                ? activityLogRepository.findByProjectIdAndInvoicedFalse(projectId)
                : activityLogRepository.findByProjectId(projectId);

        return logs.stream()
                .map(ActivityLogResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
