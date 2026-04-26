package com.dukaanpe.billing.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String hsnCode;
    private Double quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal mrp;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal totalAmount;
}

