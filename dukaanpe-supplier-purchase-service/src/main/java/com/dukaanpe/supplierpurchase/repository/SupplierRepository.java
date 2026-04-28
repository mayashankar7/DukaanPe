package com.dukaanpe.supplierpurchase.repository;

import com.dukaanpe.supplierpurchase.entity.Supplier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByStoreIdAndIsActiveTrueOrderBySupplierNameAsc(Long storeId);

    List<Supplier> findByStoreIdAndIsActiveTrueAndSupplierNameContainingIgnoreCaseOrderBySupplierNameAsc(Long storeId, String q);

    Optional<Supplier> findFirstByStoreIdAndIsActiveTrueOrderBySupplierNameAsc(Long storeId);
}

