package com.dukaanpe.udhar.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettlementMonthlyReportResponse {

    private Long storeId;
    private String month;
    private BigDecimal totalAmountSettled;
    private BigDecimal totalDiscountGiven;
    private long totalRecords;
    private PagedResponse<SettlementResponse> settlements;
}

