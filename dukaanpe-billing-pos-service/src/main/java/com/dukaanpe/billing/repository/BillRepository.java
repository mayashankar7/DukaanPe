package com.dukaanpe.billing.repository;

import com.dukaanpe.billing.entity.Bill;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Page<Bill> findByStoreIdAndBillingDateBetweenOrderByBillingDateDesc(
        Long storeId,
        LocalDateTime start,
        LocalDateTime end,
        Pageable pageable
    );

    Page<Bill> findByStoreIdOrderByBillingDateDesc(Long storeId, Pageable pageable);

    Optional<Bill> findByBillNumber(String billNumber);

    List<Bill> findByStoreIdAndBillingDateBetween(Long storeId, LocalDateTime start, LocalDateTime end);

    Page<Bill> findByStoreIdAndBillNumberContainingIgnoreCaseOrStoreIdAndCustomerNameContainingIgnoreCaseOrderByBillingDateDesc(
        Long storeId1,
        String billNumber,
        Long storeId2,
        String customerName,
        Pageable pageable
    );

    long countByStoreIdAndBillingDateBetween(Long storeId, LocalDateTime start, LocalDateTime end);
}

