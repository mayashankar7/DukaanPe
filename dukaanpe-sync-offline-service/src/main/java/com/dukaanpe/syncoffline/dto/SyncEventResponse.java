package com.dukaanpe.syncoffline.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SyncEventResponse {

    private Long id;
    private Long storeId;
    private String entityType;
    private String entityId;
    private String operation;
    private String payload;
    private LocalDateTime createdAt;
}

