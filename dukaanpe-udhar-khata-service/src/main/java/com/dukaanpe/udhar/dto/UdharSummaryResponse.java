package com.dukaanpe.udhar.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UdharSummaryResponse {

    private Long storeId;
    private BigDecimal totalOutstanding;
    private BigDecimal totalCreditGiven;
    private BigDecimal totalCollected;
    private long activeCustomers;
}

