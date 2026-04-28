package com.dukaanpe.supplierpurchase.dto;

import com.dukaanpe.supplierpurchase.entity.PurchaseOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePurchaseOrderStatusRequest {

    @NotNull(message = "status is required")
    private PurchaseOrderStatus status;
}

