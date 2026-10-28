package com.accounting.revenuerecognition.domain.model;

import com.accounting.revenuerecognition.domain.enums.MilestoneStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contract_milestones", indexes = {
    @Index(name = "idx_milestone_project", columnList = "project_id"),
    @Index(name = "idx_milestone_status", columnList = "status")
})
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ContractProject project;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Fixed dollar amount deliverable tied to this milestone.
     */
    @Column(name = "deliverable_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal deliverableAmount;

    /**
     * Percentage weight of this milestone relative to the total project contract (0 to 100).
     */
    @Column(name = "completion_percentage", precision = 5, scale = 2)
    private BigDecimal completionPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MilestoneStatus status;

    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;

    @Column(name = "actual_completion_date")
    private LocalDate actualCompletionDate;

    public Milestone() {
        this.status = MilestoneStatus.PENDING;
    }

    public Milestone(ContractProject project, String title, String description,
                     BigDecimal deliverableAmount, BigDecimal completionPercentage,
                     LocalDate targetCompletionDate) {
        this.project = project;
        this.title = title;
        this.description = description;
        this.deliverableAmount = deliverableAmount;
        this.completionPercentage = completionPercentage;
        this.targetCompletionDate = targetCompletionDate;
        this.status = MilestoneStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContractProject getProject() {
        return project;
    }

    public void setProject(ContractProject project) {
        this.project = project;
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
