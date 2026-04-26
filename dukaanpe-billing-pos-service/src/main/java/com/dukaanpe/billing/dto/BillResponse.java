package com.dukaanpe.billing.dto;

import com.dukaanpe.billing.entity.BillStatus;
import com.dukaanpe.billing.entity.PaymentMode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {

    private Long id;
    private Long storeId;
    private String billNumber;
    private String customerPhone;
    private String customerName;
    private LocalDateTime billingDate;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalGst;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal roundOff;
    private BigDecimal grandTotal;
    private PaymentMode paymentMode;
    private BigDecimal cashAmount;
    private BigDecimal upiAmount;
    private BigDecimal cardAmount;
    private BigDecimal creditAmount;
    private Boolean isCredit;
    private Boolean isGstBill;
    private String gstinCustomer;
    private BillStatus status;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<BillItemResponse> items;
}

