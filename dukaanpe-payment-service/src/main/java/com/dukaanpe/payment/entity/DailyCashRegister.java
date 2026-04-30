package com.dukaanpe.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "daily_cash_register")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCashRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "terminal_id", nullable = false, length = 40)
    private String terminalId;

    @Column(name = "register_date", nullable = false)
    private LocalDate registerDate;

    @Column(name = "opening_balance", precision = 12, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "total_cash_received", precision = 12, scale = 2)
    private BigDecimal totalCashReceived;

    @Column(name = "total_cash_paid", precision = 12, scale = 2)
    private BigDecimal totalCashPaid;

    @Column(name = "closing_balance", precision = 12, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "actual_cash_in_drawer", precision = 12, scale = 2)
    private BigDecimal actualCashInDrawer;

    @Column(precision = 12, scale = 2)
    private BigDecimal difference;

    @Column(name = "closed_by")
    private String closedBy;

    @Column(name = "is_closed")
    private Boolean isClosed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (terminalId == null || terminalId.isBlank()) {
            terminalId = "DEFAULT";
        }
        if (isClosed == null) {
            isClosed = false;
        }
    }
}

