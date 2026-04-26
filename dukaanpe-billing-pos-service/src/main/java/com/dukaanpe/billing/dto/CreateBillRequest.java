package com.dukaanpe.billing.dto;

import com.dukaanpe.billing.entity.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CreateBillRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "customerPhone must be a valid 10 digit Indian mobile")
    private String customerPhone;

    private String customerName;

    @NotEmpty(message = "At least one bill item is required")
    @Valid
    private List<BillItemRequest> items;

    @NotNull(message = "paymentMode is required")
    private PaymentMode paymentMode;

    private BigDecimal cashAmount;
    private BigDecimal upiAmount;
    private BigDecimal cardAmount;
    private BigDecimal creditAmount;
    private String gstinCustomer;
    private Boolean isGstBill;
    private String notes;
    private String createdBy;
}

