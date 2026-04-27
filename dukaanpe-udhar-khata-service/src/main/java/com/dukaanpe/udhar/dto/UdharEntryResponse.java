package com.dukaanpe.udhar.dto;

import com.dukaanpe.udhar.entity.EntryType;
import com.dukaanpe.udhar.entity.UdharPaymentMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UdharEntryResponse {

    private Long id;
    private Long storeId;
    private Long khataCustomerId;
    private String customerName;
    private Long billId;
    private EntryType entryType;
    private BigDecimal amount;
    private BigDecimal runningBalance;
    private String description;
    private String itemsSummary;
    private LocalDate dueDate;
    private UdharPaymentMode paymentMode;
    private String referenceNumber;
    private String createdBy;
    private LocalDateTime createdAt;
    private boolean creditLimitBreached;
}

