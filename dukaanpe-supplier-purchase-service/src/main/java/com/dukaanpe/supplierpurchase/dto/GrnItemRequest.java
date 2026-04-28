package com.dukaanpe.supplierpurchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class GrnItemRequest {

    private Long productId;

    @NotBlank(message = "productName is required")
    private String productName;

    @NotNull(message = "quantityReceived is required")
    @Positive(message = "quantityReceived must be positive")
    private Double quantityReceived;

    @NotNull(message = "quantityAccepted is required")
    @Positive(message = "quantityAccepted must be positive")
    private Double quantityAccepted;

    @DecimalMin(value = "0.0", message = "quantityRejected must be >= 0")
    private Double quantityRejected;

    private String rejectionReason;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "unitPrice must be positive")
    private BigDecimal unitPrice;
}

