package com.dukaanpe.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private Long storeId;
    private String categoryName;
    private String categoryNameHindi;
    private Long parentCategoryId;
    private Integer displayOrder;
    private Boolean isActive;
}

