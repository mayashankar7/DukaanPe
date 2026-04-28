package com.dukaanpe.supplierpurchase.dto;

import com.dukaanpe.supplierpurchase.entity.GrnStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GrnResponse {

    private Long id;
    private Long storeId;
    private Long purchaseOrderId;
    private String grnNumber;
    private LocalDate receivedDate;
    private String supplierInvoiceNumber;
    private LocalDate supplierInvoiceDate;
    private BigDecimal totalAmount;
    private GrnStatus status;
    private String receivedBy;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<GrnItemResponse> items;
}

