package com.dukaanpe.syncoffline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SyncPushRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotBlank(message = "entityType is required")
    private String entityType;

    @NotBlank(message = "entityId is required")
    private String entityId;

    @NotBlank(message = "operation is required")
    private String operation;

    @NotBlank(message = "payload is required")
    private String payload;
}

