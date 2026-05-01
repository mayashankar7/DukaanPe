package com.dukaanpe.customerloyalty.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(nullable = false, length = 10)
    private String phone;

    private String email;

    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "anniversary_date")
    private LocalDate anniversaryDate;

    @Column(name = "total_purchases", nullable = false)
    private BigDecimal totalPurchases;

    @Column(name = "total_visits", nullable = false)
    private Integer totalVisits;

    @Column(name = "last_visit_date")
    private LocalDate lastVisitDate;

    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier", nullable = false)
    private CustomerTier customerTier;

    private String tags;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (totalPurchases == null) {
            totalPurchases = BigDecimal.ZERO;
        }
        if (totalVisits == null) {
            totalVisits = 0;
        }
        if (loyaltyPoints == null) {
            loyaltyPoints = 0;
        }
        if (customerTier == null) {
            customerTier = CustomerTier.REGULAR;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

