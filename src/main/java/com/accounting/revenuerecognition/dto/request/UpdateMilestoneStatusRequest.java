package com.accounting.revenuerecognition.dto.request;

import com.accounting.revenuerecognition.domain.enums.MilestoneStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class UpdateMilestoneStatusRequest {

    @NotNull(message = "Milestone status is required")
    private MilestoneStatus status;

    private LocalDate actualCompletionDate;

    public UpdateMilestoneStatusRequest() {
    }

    public MilestoneStatus getStatus() {
        return status;
    }

    public void setStatus(MilestoneStatus status) {
        this.status = status;
    }

    public LocalDate getActualCompletionDate() {
        return actualCompletionDate;
    }

    public void setActualCompletionDate(LocalDate actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
    }
}
