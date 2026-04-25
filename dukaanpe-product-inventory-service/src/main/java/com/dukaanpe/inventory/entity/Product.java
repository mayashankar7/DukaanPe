package com.dukaanpe.inventory.entity;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_name_hindi")
    private String productNameHindi;

    @Column(name = "product_name_regional")
    private String productNameRegional;

    private String barcode;

    private String sku;

    private String description;

    @Column(name = "hsn_code")
    private String hsnCode;

    private String brand;

    private BigDecimal mrp;

    @Column(name = "selling_price", nullable = false)
    private BigDecimal sellingPrice;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductUnit unit;

    @Builder.Default
    @Column(name = "unit_quantity", nullable = false)
    private Double unitQuantity = 1.0;

    @Column(name = "gst_rate")
    private BigDecimal gstRate;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.unit == null) {
            this.unit = ProductUnit.PIECE;
        }
        if (this.unitQuantity == null) {
            this.unitQuantity = 1.0;
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

