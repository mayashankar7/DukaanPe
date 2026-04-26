package com.dukaanpe.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UdharCreditRequest {

    private Long storeId;
    private String customerPhone;
    private Long billId;
    private BigDecimal amount;
    private String description;
    private String itemsSummary;
    private LocalDate dueDate;
    private String createdBy;
}

