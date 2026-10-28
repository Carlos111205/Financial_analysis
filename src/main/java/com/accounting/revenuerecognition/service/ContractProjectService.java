package com.accounting.revenuerecognition.service;

import com.accounting.revenuerecognition.domain.enums.MilestoneStatus;
import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.enums.ProjectStatus;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import com.accounting.revenuerecognition.domain.model.Milestone;
import com.accounting.revenuerecognition.dto.request.CreateMilestoneRequest;
import com.accounting.revenuerecognition.dto.request.CreateProjectRequest;
import com.accounting.revenuerecognition.dto.request.UpdateMilestoneStatusRequest;
import com.accounting.revenuerecognition.dto.request.UpdateProjectStatusRequest;
import com.accounting.revenuerecognition.dto.response.MilestoneResponse;
import com.accounting.revenuerecognition.dto.response.ProjectResponse;
import com.accounting.revenuerecognition.exception.InvalidRevenueOperationException;
import com.accounting.revenuerecognition.exception.ResourceNotFoundException;
import com.accounting.revenuerecognition.repository.ContractProjectRepository;
import com.accounting.revenuerecognition.repository.MilestoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContractProjectService {

    private static final Logger log = LoggerFactory.getLogger(ContractProjectService.class);

    private final ContractProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;

    public ContractProjectService(ContractProjectRepository projectRepository,
                                  MilestoneRepository milestoneRepository) {
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
    }

    public ProjectResponse createProject(CreateProjectRequest request) {
        if (projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new InvalidRevenueOperationException("Project with code '" + request.getProjectCode() + "' already exists");
        }

        // Validate model-specific mandatory attributes
        if (request.getPricingModel() == PricingModel.FIXED_PRICE) {
            if (request.getTotalContractValue() == null || request.getTotalContractValue().signum() <= 0) {
                throw new InvalidRevenueOperationException("Fixed-Price contracts require a positive total contract value");
            }
        } else if (request.getPricingModel() == PricingModel.TIME_AND_MATERIALS) {
            if (request.getDefaultHourlyRate() == null || request.getDefaultHourlyRate().signum() <= 0) {
                throw new InvalidRevenueOperationException("Time-and-Materials contracts require a positive default hourly rate");
            }
        }

        ContractProject project = new ContractProject();
        project.setProjectCode(request.getProjectCode());
        project.setName(request.getName());
        project.setClientName(request.getClientName());
        project.setPricingModel(request.getPricingModel());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setTotalContractValue(request.getTotalContractValue());
        project.setDefaultHourlyRate(request.getDefaultHourlyRate());
        project.setCostMarkupPercentage(request.getCostMarkupPercentage());
        project.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        project.setEndDate(request.getEndDate());

        ContractProject saved = projectRepository.save(project);
        log.info("Created project ID: {} with code: {} and model: {}", saved.getId(), saved.getProjectCode(), saved.getPricingModel());
        return ProjectResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        ContractProject project = projectRepository.findByIdWithMilestones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract Project not found with ID: " + id));
        return ProjectResponse.fromEntity(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(PricingModel pricingModel, ProjectStatus status) {
        List<ContractProject> projects;
        if (pricingModel != null) {
            projects = projectRepository.findByPricingModel(pricingModel);
        } else if (status != null) {
            projects = projectRepository.findByStatus(status);
        } else {
            projects = projectRepository.findAll();
        }

        return projects.stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ProjectResponse updateProjectStatus(Long id, UpdateProjectStatusRequest request) {
        ContractProject project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract Project not found with ID: " + id));

        project.setStatus(request.getStatus());
        ContractProject saved = projectRepository.save(project);
        log.info("Updated project ID: {} to status: {}", id, request.getStatus());
        return ProjectResponse.fromEntity(saved);
    }

    public MilestoneResponse addMilestone(Long projectId, CreateMilestoneRequest request) {
        ContractProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract Project not found with ID: " + projectId));

        if (project.getPricingModel() != PricingModel.FIXED_PRICE) {
            throw new InvalidRevenueOperationException("Milestones can only be added to FIXED_PRICE contracts. Current project model is: " + project.getPricingModel());
        }

        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setDeliverableAmount(request.getDeliverableAmount());
        milestone.setCompletionPercentage(request.getCompletionPercentage());
        milestone.setTargetCompletionDate(request.getTargetCompletionDate());
        milestone.setStatus(MilestoneStatus.PENDING);

        project.addMilestone(milestone);
        Milestone saved = milestoneRepository.save(milestone);
        log.info("Added milestone ID: {} to project: {}", saved.getId(), project.getProjectCode());
        return MilestoneResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> getMilestones(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Contract Project not found with ID: " + projectId);
        }
        return milestoneRepository.findByProjectId(projectId).stream()
                .map(MilestoneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public MilestoneResponse updateMilestoneStatus(Long projectId, Long milestoneId, UpdateMilestoneStatusRequest request) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + milestoneId));

        if (!milestone.getProject().getId().equals(projectId)) {
            throw new InvalidRevenueOperationException("Milestone ID " + milestoneId + " does not belong to project ID " + projectId);
        }

        milestone.setStatus(request.getStatus());
        if (request.getStatus() == MilestoneStatus.COMPLETED) {
            milestone.setActualCompletionDate(request.getActualCompletionDate() != null
                    ? request.getActualCompletionDate()
                    : LocalDate.now());
        }

        Milestone saved = milestoneRepository.save(milestone);
        log.info("Updated milestone ID: {} status to: {}", milestoneId, request.getStatus());
        return MilestoneResponse.fromEntity(saved);
    }
}
