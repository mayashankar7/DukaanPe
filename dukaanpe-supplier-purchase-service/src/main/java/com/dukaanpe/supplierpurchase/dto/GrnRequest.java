package com.dukaanpe.supplierpurchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class GrnRequest {

    @NotNull(message = "storeId is required")
    private Long storeId;

    @NotNull(message = "purchaseOrderId is required")
    private Long purchaseOrderId;

    @NotNull(message = "receivedDate is required")
    private LocalDate receivedDate;

    private String supplierInvoiceNumber;
    private LocalDate supplierInvoiceDate;
    private String receivedBy;
    private String notes;

    @NotEmpty(message = "items are required")
    @Valid
    private List<GrnItemRequest> items;
}

