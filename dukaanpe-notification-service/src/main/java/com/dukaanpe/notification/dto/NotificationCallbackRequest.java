package com.dukaanpe.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationCallbackRequest {

    @NotBlank(message = "providerMessageId is required")
    private String providerMessageId;

    @NotBlank(message = "status is required")
    private String status;

    private String failureReason;
}

