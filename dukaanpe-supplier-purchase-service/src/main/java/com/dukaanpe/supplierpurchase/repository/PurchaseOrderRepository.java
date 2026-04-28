package com.dukaanpe.supplierpurchase.repository;

import com.dukaanpe.supplierpurchase.entity.PurchaseOrder;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Page<PurchaseOrder> findByStoreIdOrderByOrderDateDescIdDesc(Long storeId, Pageable pageable);

    long countByOrderDate(LocalDate orderDate);

    List<PurchaseOrder> findTop20ByStoreIdOrderByOrderDateDescIdDesc(Long storeId);

    @Query("""
        select po from PurchaseOrder po
        join po.items item
        where po.storeId = :storeId
          and lower(item.productName) = lower(:productName)
        order by po.orderDate desc, po.id desc
        """)
    List<PurchaseOrder> findRecentByStoreAndProductName(
        @Param("storeId") Long storeId,
        @Param("productName") String productName,
        Pageable pageable
    );
}

