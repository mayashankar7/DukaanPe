package com.dukaanpe.gsttax.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GstInvoiceItemResponse {

    private Long id;
    private String hsnCode;
    private String itemDescription;
    private BigDecimal quantity;
    private BigDecimal taxableValue;
    private BigDecimal gstRate;
    private BigDecimal cessRate;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal cessAmount;
    private BigDecimal totalTax;
    private BigDecimal lineTotal;
}

