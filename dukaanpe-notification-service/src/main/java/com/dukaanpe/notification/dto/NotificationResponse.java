package com.dukaanpe.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private Long storeId;
    private String channel;
    private String recipient;
    private String title;
    private String message;
    private String status;
    private LocalDateTime sentAt;
}

