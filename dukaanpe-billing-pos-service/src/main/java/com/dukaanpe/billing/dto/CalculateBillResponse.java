package com.dukaanpe.billing.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateBillResponse {

    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalGst;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal roundOff;
    private BigDecimal grandTotal;
    private List<BillItemResponse> items;
}

