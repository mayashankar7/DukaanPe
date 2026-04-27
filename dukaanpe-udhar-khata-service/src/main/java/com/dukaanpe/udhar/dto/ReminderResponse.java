package com.dukaanpe.udhar.dto;

import com.dukaanpe.udhar.entity.ReminderType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReminderResponse {

    private Long id;
    private Long udharEntryId;
    private Long khataCustomerId;
    private String customerName;
    private Long storeId;
    private LocalDate reminderDate;
    private ReminderType reminderType;
    private String message;
    private Boolean isSent;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}

