package com.dukaanpe.udhar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khata_customer_id", nullable = false)
    private KhataCustomer khataCustomer;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "total_outstanding_before", nullable = false)
    private BigDecimal totalOutstandingBefore;

    @Column(name = "amount_settled", nullable = false)
    private BigDecimal amountSettled;

    @Column(name = "discount_given", nullable = false)
    @Builder.Default
    private BigDecimal discountGiven = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_mode", nullable = false)
    private SettlementMode settlementMode;

    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (discountGiven == null) {
            discountGiven = BigDecimal.ZERO;
        }
    }
}

