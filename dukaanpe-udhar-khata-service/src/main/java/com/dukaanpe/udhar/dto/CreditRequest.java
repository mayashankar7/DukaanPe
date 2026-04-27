package com.dukaanpe.udhar.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreditRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @Positive(message = "khataCustomerId must be positive")
    private Long khataCustomerId;

    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "customerPhone must be a valid 10 digit Indian mobile")
    private String customerPhone;

    private Long billId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    private String description;
    private String itemsSummary;
    private LocalDate dueDate;
    private String createdBy;
}

