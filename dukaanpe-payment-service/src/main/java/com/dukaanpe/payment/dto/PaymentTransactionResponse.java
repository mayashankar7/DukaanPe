package com.dukaanpe.payment.dto;

import com.dukaanpe.payment.entity.PaymentMode;
import com.dukaanpe.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentTransactionResponse {

    private Long id;
    private Long storeId;
    private String transactionId;
    private Long billId;
    private Long udharEntryId;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private PaymentStatus paymentStatus;
    private String upiId;
    private String upiReference;
    private String cardLastFour;
    private String payerName;
    private String payerPhone;
    private String description;
    private String terminalId;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

