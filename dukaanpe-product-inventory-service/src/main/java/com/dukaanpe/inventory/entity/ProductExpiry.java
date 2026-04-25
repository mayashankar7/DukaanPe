package com.dukaanpe.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_expiry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductExpiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Double quantity;

    @Builder.Default
    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired = false;

    @Builder.Default
    @Column(name = "alert_sent", nullable = false)
    private Boolean alertSent = false;

    @PrePersist
    public void prePersist() {
        if (this.expiryDate != null && this.expiryDate.isBefore(LocalDate.now())) {
            this.isExpired = true;
        }
        if (this.alertSent == null) {
            this.alertSent = false;
        }
        if (this.isExpired == null) {
            this.isExpired = false;
        }
    }
}

