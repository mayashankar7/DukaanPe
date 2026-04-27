package com.dukaanpe.udhar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KhataCustomerResponse {

    private Long id;
    private Long storeId;
    private String customerName;
    private String customerPhone;
    private String address;
    private BigDecimal creditLimit;
    private BigDecimal totalOutstanding;
    private BigDecimal totalCreditGiven;
    private BigDecimal totalCollected;
    private Integer trustScore;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

