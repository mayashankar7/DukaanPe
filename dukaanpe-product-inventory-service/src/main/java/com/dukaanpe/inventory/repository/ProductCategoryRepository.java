package com.dukaanpe.inventory.repository;

import com.dukaanpe.inventory.entity.ProductCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(Long storeId);
}

