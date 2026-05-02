package com.dukaanpe.gsttax.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TaxSummaryResponse {

    private Long storeId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private long invoiceCount;
    private BigDecimal taxableAmount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal cessAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalInvoiceAmount;
    private List<TaxRateBreakupResponse> taxRateBreakup;
}

