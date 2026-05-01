package com.dukaanpe.customerloyalty.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateCustomerRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotBlank(message = "customerName is required")
    @Size(max = 120, message = "customerName must be <= 120 chars")
    private String customerName;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "phone must be a valid 10 digit Indian mobile")
    private String phone;

    @Email(message = "email must be valid")
    @Size(max = 160, message = "email must be <= 160 chars")
    private String email;

    @Size(max = 300, message = "address must be <= 300 chars")
    private String address;

    private LocalDate dateOfBirth;

    private LocalDate anniversaryDate;

    @Size(max = 200, message = "tags must be <= 200 chars")
    private String tags;
}

