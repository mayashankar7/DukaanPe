package com.dukaanpe.udhar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "khata_customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhataCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 10)
    private String customerPhone;

    private String address;

    @Column(name = "credit_limit", nullable = false)
    @Builder.Default
    private BigDecimal creditLimit = new BigDecimal("5000");

    @Column(name = "total_outstanding", nullable = false)
    @Builder.Default
    private BigDecimal totalOutstanding = BigDecimal.ZERO;

    @Column(name = "total_credit_given", nullable = false)
    @Builder.Default
    private BigDecimal totalCreditGiven = BigDecimal.ZERO;

    @Column(name = "total_collected", nullable = false)
    @Builder.Default
    private BigDecimal totalCollected = BigDecimal.ZERO;

    @Column(name = "trust_score", nullable = false)
    @Builder.Default
    private Integer trustScore = 5;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.creditLimit == null) {
            this.creditLimit = new BigDecimal("5000");
        }
        if (this.totalOutstanding == null) {
            this.totalOutstanding = BigDecimal.ZERO;
        }
        if (this.totalCreditGiven == null) {
            this.totalCreditGiven = BigDecimal.ZERO;
        }
        if (this.totalCollected == null) {
            this.totalCollected = BigDecimal.ZERO;
        }
        if (this.trustScore == null) {
            this.trustScore = 5;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

