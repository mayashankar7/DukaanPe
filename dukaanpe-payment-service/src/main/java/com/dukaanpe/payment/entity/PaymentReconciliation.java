package com.dukaanpe.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payment_reconciliation",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_reconciliation_store_date", columnNames = {"store_id", "recon_date"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "recon_date", nullable = false)
    private LocalDate reconDate;

    @Column(name = "total_cash", precision = 12, scale = 2)
    private BigDecimal totalCash;

    @Column(name = "total_upi", precision = 12, scale = 2)
    private BigDecimal totalUpi;

    @Column(name = "total_card", precision = 12, scale = 2)
    private BigDecimal totalCard;

    @Column(name = "total_collections", precision = 12, scale = 2)
    private BigDecimal totalCollections;

    @Column(name = "cash_in_hand", precision = 12, scale = 2)
    private BigDecimal cashInHand;

    @Column(precision = 12, scale = 2)
    private BigDecimal discrepancy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReconciliationStatus status;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}

