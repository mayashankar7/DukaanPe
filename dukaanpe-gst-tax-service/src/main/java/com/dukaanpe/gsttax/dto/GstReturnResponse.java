package com.dukaanpe.gsttax.dto;

import com.dukaanpe.gsttax.entity.ReturnType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class GstReturnResponse {

    private Long id;
    private Long storeId;
    private ReturnType returnType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalInvoices;
    private BigDecimal taxableAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalTaxLiability;
    private LocalDateTime generatedAt;
}

