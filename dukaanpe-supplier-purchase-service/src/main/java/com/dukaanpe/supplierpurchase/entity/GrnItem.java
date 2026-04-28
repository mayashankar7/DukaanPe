package com.dukaanpe.supplierpurchase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grn_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    private GoodsReceivedNote grn;

    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Double quantityReceived;

    @Column(nullable = false)
    private Double quantityAccepted;

    @Builder.Default
    @Column(nullable = false)
    private Double quantityRejected = 0.0;

    private String rejectionReason;

    private String batchNumber;

    private LocalDate manufacturingDate;

    private LocalDate expiryDate;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalAmount;
}

