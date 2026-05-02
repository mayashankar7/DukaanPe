package com.dukaanpe.gsttax.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GstInvoiceItemRequest {

    @NotBlank(message = "hsnCode is required")
    @Pattern(regexp = "^[0-9]{4,8}$", message = "hsnCode must be 4 to 8 digits")
    private String hsnCode;

    @NotBlank(message = "itemDescription is required")
    private String itemDescription;

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.001", message = "quantity must be > 0")
    @Digits(integer = 8, fraction = 3, message = "quantity must have up to 3 decimal places")
    private BigDecimal quantity;

    @NotNull(message = "taxableValue is required")
    @DecimalMin(value = "0.01", message = "taxableValue must be > 0")
    @Digits(integer = 12, fraction = 2, message = "taxableValue must have up to 2 decimal places")
    private BigDecimal taxableValue;
}

