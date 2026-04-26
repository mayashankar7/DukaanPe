package com.dukaanpe.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class BillItemRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotBlank(message = "productName is required")
    private String productName;

    private String hsnCode;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    private Double quantity;

    @NotBlank(message = "unit is required")
    private String unit;

    @NotNull(message = "unitPrice is required")
    @Positive(message = "unitPrice must be positive")
    private BigDecimal unitPrice;

    @NotNull(message = "mrp is required")
    @Positive(message = "mrp must be positive")
    private BigDecimal mrp;

    @PositiveOrZero(message = "discountPercent must be zero or positive")
    private BigDecimal discountPercent;

    @PositiveOrZero(message = "gstRate must be zero or positive")
    private BigDecimal gstRate;
}

