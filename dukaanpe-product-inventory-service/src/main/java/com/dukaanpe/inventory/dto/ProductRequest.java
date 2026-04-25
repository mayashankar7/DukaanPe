package com.dukaanpe.inventory.dto;

import com.dukaanpe.inventory.entity.ProductUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductRequest {

    @NotNull(message = "Store id is required")
    @Positive(message = "Store id must be positive")
    private Long storeId;

    private Long categoryId;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String productNameHindi;
    private String productNameRegional;
    private String barcode;
    private String sku;
    private String description;
    private String hsnCode;
    private String brand;

    @PositiveOrZero(message = "MRP must be positive or zero")
    private BigDecimal mrp;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be positive")
    private BigDecimal sellingPrice;

    @PositiveOrZero(message = "Purchase price must be positive or zero")
    private BigDecimal purchasePrice;

    @NotNull(message = "Unit is required")
    private ProductUnit unit;

    @Positive(message = "Unit quantity must be positive")
    private Double unitQuantity;

    @PositiveOrZero(message = "GST rate must be positive or zero")
    private BigDecimal gstRate;

    private String imageUrl;
}

