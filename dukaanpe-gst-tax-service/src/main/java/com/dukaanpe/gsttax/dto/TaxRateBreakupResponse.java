package com.dukaanpe.gsttax.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TaxRateBreakupResponse {

    private BigDecimal gstRate;
    private BigDecimal taxableAmount;
    private BigDecimal totalTaxAmount;
}

