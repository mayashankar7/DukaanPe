package com.dukaanpe.inventory.repository;

import com.dukaanpe.inventory.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStoreIdAndIsActiveTrue(Long storeId, Pageable pageable);

    List<Product> findByStoreIdAndIsActiveTrueAndCategoryId(Long storeId, Long categoryId);

    Optional<Product> findByStoreIdAndBarcodeAndIsActiveTrue(Long storeId, String barcode);

    @Query("""
        select p from Product p
        where p.storeId = :storeId
          and p.isActive = true
          and (:categoryId is null or p.category.id = :categoryId)
          and (
               lower(p.productName) like lower(concat('%', :q, '%'))
            or lower(coalesce(p.barcode, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(p.sku, '')) like lower(concat('%', :q, '%'))
          )
        """)
    Page<Product> searchProducts(
        @Param("storeId") Long storeId,
        @Param("q") String query,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );
}

