package com.dukaanpe.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotNull(message = "Store id is required")
    @Positive(message = "Store id must be positive")
    private Long storeId;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private String categoryNameHindi;
    private Long parentCategoryId;
    private Integer displayOrder;
}

