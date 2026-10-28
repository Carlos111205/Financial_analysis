package com.accounting.revenuerecognition.dto.request;

import com.accounting.revenuerecognition.domain.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateProjectStatusRequest {

    @NotNull(message = "Status is required")
    private ProjectStatus status;

    public UpdateProjectStatusRequest() {
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
