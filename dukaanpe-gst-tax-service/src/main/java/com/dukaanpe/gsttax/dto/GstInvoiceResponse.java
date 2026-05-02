package com.dukaanpe.gsttax.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GstInvoiceResponse {

    private Long id;
    private Long storeId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String customerName;
    private String customerGstin;
    private String placeOfSupply;
    private Boolean intraState;
    private BigDecimal taxableAmount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal cessAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal invoiceTotal;
    private List<GstInvoiceItemResponse> items;
    private LocalDateTime createdAt;
}

