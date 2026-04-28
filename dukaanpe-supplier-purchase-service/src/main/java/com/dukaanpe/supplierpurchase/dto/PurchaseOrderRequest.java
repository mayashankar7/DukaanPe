package com.dukaanpe.supplierpurchase.dto;

import com.dukaanpe.supplierpurchase.entity.PurchaseOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class PurchaseOrderRequest {

    @NotNull(message = "storeId is required")
    private Long storeId;

    @NotNull(message = "supplierId is required")
    private Long supplierId;

    @NotNull(message = "orderDate is required")
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private PurchaseOrderStatus status;

    private String notes;

    private String createdBy;

    @NotEmpty(message = "items are required")
    @Valid
    private List<PurchaseOrderItemRequest> items;
}

