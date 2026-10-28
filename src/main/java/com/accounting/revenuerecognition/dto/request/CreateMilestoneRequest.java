package com.accounting.revenuerecognition.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateMilestoneRequest {

    @NotBlank(message = "Milestone title is required")
    private String title;

    private String description;

    @NotNull(message = "Deliverable amount is required")
    @DecimalMin(value = "0.01", message = "Deliverable amount must be greater than zero")
    private BigDecimal deliverableAmount;

    @DecimalMin(value = "0.00", message = "Completion percentage must be >= 0")
    @DecimalMax(value = "100.00", message = "Completion percentage must be <= 100")
    private BigDecimal completionPercentage;

    private LocalDate targetCompletionDate;

    public CreateMilestoneRequest() {
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

    public LocalDate getTargetCompletionDate() {
        return targetCompletionDate;
    }

    public void setTargetCompletionDate(LocalDate targetCompletionDate) {
        this.targetCompletionDate = targetCompletionDate;
    }
}
