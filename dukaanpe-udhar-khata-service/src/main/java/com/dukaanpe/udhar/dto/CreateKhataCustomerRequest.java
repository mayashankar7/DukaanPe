package com.dukaanpe.udhar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateKhataCustomerRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotBlank(message = "customerName is required")
    private String customerName;

    @NotBlank(message = "customerPhone is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "customerPhone must be a valid 10 digit Indian mobile")
    private String customerPhone;

    private String address;
    private BigDecimal creditLimit;
    private String notes;
}

