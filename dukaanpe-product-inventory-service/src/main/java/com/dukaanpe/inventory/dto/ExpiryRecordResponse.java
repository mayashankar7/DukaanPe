package com.dukaanpe.inventory.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpiryRecordResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long storeId;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private Double quantity;
    private Boolean isExpired;
    private Boolean alertSent;
}

