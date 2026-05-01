package com.dukaanpe.customerloyalty.repository;

import com.dukaanpe.customerloyalty.entity.LoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    Page<LoyaltyTransaction> findByCustomerIdOrderByCreatedAtDescIdDesc(Long customerId, Pageable pageable);
}

