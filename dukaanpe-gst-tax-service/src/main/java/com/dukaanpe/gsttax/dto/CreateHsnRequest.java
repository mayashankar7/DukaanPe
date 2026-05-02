package com.dukaanpe.gsttax.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateHsnRequest {

    @NotBlank(message = "hsnCode is required")
    @Pattern(regexp = "^[0-9]{4,8}$", message = "hsnCode must be 4 to 8 digits")
    private String hsnCode;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "gstRate is required")
    @DecimalMin(value = "0.00", message = "gstRate must be >= 0")
    @Digits(integer = 3, fraction = 2, message = "gstRate must have up to 2 decimal places")
    private BigDecimal gstRate;

    @DecimalMin(value = "0.00", message = "cessRate must be >= 0")
    @Digits(integer = 3, fraction = 2, message = "cessRate must have up to 2 decimal places")
    private BigDecimal cessRate;
}

