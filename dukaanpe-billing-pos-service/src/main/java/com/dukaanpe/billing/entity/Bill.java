package com.dukaanpe.billing.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "bill_number", nullable = false, unique = true)
    private String billNumber;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "billing_date", nullable = false)
    private LocalDateTime billingDate;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "total_discount", nullable = false)
    private BigDecimal totalDiscount;

    @Column(name = "total_gst", nullable = false)
    private BigDecimal totalGst;

    @Column(nullable = false)
    private BigDecimal cgst;

    @Column(nullable = false)
    private BigDecimal sgst;

    @Column(nullable = false)
    private BigDecimal igst;

    @Column(name = "round_off", nullable = false)
    private BigDecimal roundOff;

    @Column(name = "grand_total", nullable = false)
    private BigDecimal grandTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode;

    @Builder.Default
    @Column(name = "cash_amount", nullable = false)
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "upi_amount", nullable = false)
    private BigDecimal upiAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "card_amount", nullable = false)
    private BigDecimal cardAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "credit_amount", nullable = false)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_credit", nullable = false)
    private Boolean isCredit = false;

    @Builder.Default
    @Column(name = "is_gst_bill", nullable = false)
    private Boolean isGstBill = false;

    @Column(name = "gstin_customer")
    private String gstinCustomer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status;

    private String notes;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BillItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.billingDate == null) {
            this.billingDate = LocalDateTime.now();
        }
        if (this.totalDiscount == null) {
            this.totalDiscount = BigDecimal.ZERO;
        }
        if (this.igst == null) {
            this.igst = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = BillStatus.COMPLETED;
        }
    }
}

