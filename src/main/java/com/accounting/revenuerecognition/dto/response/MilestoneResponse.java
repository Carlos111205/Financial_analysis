package com.accounting.revenuerecognition.dto.response;

import com.accounting.revenuerecognition.domain.enums.MilestoneStatus;
import com.accounting.revenuerecognition.domain.model.Milestone;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MilestoneResponse {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private BigDecimal deliverableAmount;
    private BigDecimal completionPercentage;
    private MilestoneStatus status;
    private LocalDate targetCompletionDate;
    private LocalDate actualCompletionDate;

    public MilestoneResponse() {
    }

    public static MilestoneResponse fromEntity(Milestone m) {
        MilestoneResponse res = new MilestoneResponse();
        res.setId(m.getId());
        res.setProjectId(m.getProject() != null ? m.getProject().getId() : null);
        res.setTitle(m.getTitle());
        res.setDescription(m.getDescription());
        res.setDeliverableAmount(m.getDeliverableAmount());
        res.setCompletionPercentage(m.getCompletionPercentage());
        res.setStatus(m.getStatus());
        res.setTargetCompletionDate(m.getTargetCompletionDate());
        res.setActualCompletionDate(m.getActualCompletionDate());
        return res;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDeliverableAmount() {
        return deliverableAmount;
    }

    public void setDeliverableAmount(BigDecimal deliverableAmount) {
        this.deliverableAmount = deliverableAmount;
    }

    public BigDecimal getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(BigDecimal completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public MilestoneStatus getStatus() {
        return status;
    }

    public void setStatus(MilestoneStatus status) {
        this.status = status;
    }

    public LocalDate getTargetCompletionDate() {
        return targetCompletionDate;
    }

    public void setTargetCompletionDate(LocalDate targetCompletionDate) {
        this.targetCompletionDate = targetCompletionDate;
    }

    public LocalDate getActualCompletionDate() {
        return actualCompletionDate;
    }

    public void setActualCompletionDate(LocalDate actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
    }
}
