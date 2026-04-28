package com.dukaanpe.supplierpurchase.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierRequest {

    @NotNull(message = "storeId is required")
    private Long storeId;

    @NotBlank(message = "supplierName is required")
    @Size(max = 120, message = "supplierName must be at most 120 chars")
    private String supplierName;

    @Size(max = 80, message = "contactPerson must be at most 80 chars")
    private String contactPerson;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "phone must be a valid 10-digit Indian mobile")
    private String phone;

    @Pattern(regexp = "^$|^[6-9][0-9]{9}$", message = "alternatePhone must be empty or a valid 10-digit Indian mobile")
    private String alternatePhone;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "email must be valid")
    private String email;

    private String address;
    private String city;
    private String state;

    @Pattern(regexp = "^$|^[1-9][0-9]{5}$", message = "pincode must be empty or 6 digits")
    private String pincode;

    @Pattern(regexp = "^$|^[0-9A-Z]{15}$", message = "gstin must be empty or 15 uppercase alphanumeric chars")
    private String gstin;

    private String categoriesSupplied;
    private String paymentTerms;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;

    @Min(value = 1, message = "rating must be at least 1")
    @Max(value = 5, message = "rating must be at most 5")
    private Integer rating;
}

