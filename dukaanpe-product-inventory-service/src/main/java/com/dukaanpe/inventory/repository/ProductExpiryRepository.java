package com.dukaanpe.inventory.repository;

import com.dukaanpe.inventory.entity.ProductExpiry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductExpiryRepository extends JpaRepository<ProductExpiry, Long> {

    List<ProductExpiry> findByStoreIdAndExpiryDateBetweenOrderByExpiryDateAsc(Long storeId, LocalDate from, LocalDate to);

    List<ProductExpiry> findByStoreIdAndExpiryDateBeforeOrderByExpiryDateAsc(Long storeId, LocalDate date);
}

