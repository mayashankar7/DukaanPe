package com.dukaanpe.customerloyalty.repository;

import com.dukaanpe.customerloyalty.entity.PurchaseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {

    Page<PurchaseHistory> findByCustomerIdOrderByBillDateDescIdDesc(Long customerId, Pageable pageable);
}

