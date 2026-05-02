package com.dukaanpe.gsttax.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GenerateGstInvoiceRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotBlank(message = "invoiceNumber is required")
    private String invoiceNumber;

    @NotNull(message = "invoiceDate is required")
    private LocalDate invoiceDate;

    private String customerName;
    private String customerGstin;
    private String placeOfSupply;

    @NotNull(message = "intraState is required")
    private Boolean intraState;

    @Valid
    @NotEmpty(message = "items are required")
    private List<GstInvoiceItemRequest> items;
}

