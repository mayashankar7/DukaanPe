package com.dukaanpe.customerloyalty.entity;

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
@Table(name = "loyalty_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false, unique = true)
    private Long storeId;

    @Column(name = "points_per_hundred", nullable = false)
    private Integer pointsPerHundred;

    @Column(name = "points_to_redeem_unit", nullable = false)
    private Integer pointsToRedeemUnit;

    @Column(name = "redeem_value_rupees", nullable = false)
    private Integer redeemValueRupees;

    @Column(name = "silver_threshold", nullable = false)
    private BigDecimal silverThreshold;

    @Column(name = "gold_threshold", nullable = false)
    private BigDecimal goldThreshold;

    @Column(name = "platinum_threshold", nullable = false)
    private BigDecimal platinumThreshold;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (pointsPerHundred == null) {
            pointsPerHundred = 1;
        }
        if (pointsToRedeemUnit == null) {
            pointsToRedeemUnit = 100;
        }
        if (redeemValueRupees == null) {
            redeemValueRupees = 10;
        }
        if (silverThreshold == null) {
            silverThreshold = new BigDecimal("5000");
        }
        if (goldThreshold == null) {
            goldThreshold = new BigDecimal("15000");
        }
        if (platinumThreshold == null) {
            platinumThreshold = new BigDecimal("50000");
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

