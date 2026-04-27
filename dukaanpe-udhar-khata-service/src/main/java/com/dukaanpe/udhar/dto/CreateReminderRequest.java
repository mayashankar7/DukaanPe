package com.dukaanpe.udhar.dto;

import com.dukaanpe.udhar.entity.ReminderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateReminderRequest {

    @Positive(message = "udharEntryId must be positive")
    private Long udharEntryId;

    @NotNull(message = "khataCustomerId is required")
    @Positive(message = "khataCustomerId must be positive")
    private Long khataCustomerId;

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotNull(message = "reminderDate is required")
    private LocalDate reminderDate;

    @NotNull(message = "reminderType is required")
    private ReminderType reminderType;

    @NotBlank(message = "message is required")
    private String message;
}

