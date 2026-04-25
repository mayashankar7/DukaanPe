package com.dukaanpe.inventory.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ExpiryRecordRequest {

    @NotNull(message = "Product id is required")
    private Long productId;

    @NotNull(message = "Store id is required")
    private Long storeId;

    private String batchNumber;
    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry date is required")
    @FutureOrPresent(message = "Expiry date must be today or in future")
    private LocalDate expiryDate;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double quantity;
}

