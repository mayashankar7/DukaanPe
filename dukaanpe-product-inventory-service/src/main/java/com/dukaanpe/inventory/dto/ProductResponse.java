package com.dukaanpe.inventory.dto;

import com.dukaanpe.inventory.entity.ProductUnit;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private Long storeId;
    private Long categoryId;
    private String categoryName;
    private String productName;
    private String productNameHindi;
    private String productNameRegional;
    private String barcode;
    private String sku;
    private String description;
    private String hsnCode;
    private String brand;
    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private BigDecimal purchasePrice;
    private ProductUnit unit;
    private Double unitQuantity;
    private BigDecimal gstRate;
    private String imageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

