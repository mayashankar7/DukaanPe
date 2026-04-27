package com.dukaanpe.udhar.repository;

import com.dukaanpe.udhar.entity.SettlementRecord;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRecordRepository extends JpaRepository<SettlementRecord, Long> {

    Page<SettlementRecord> findByKhataCustomerIdAndSettlementDateBetweenOrderBySettlementDateDesc(
        Long customerId,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    );

    Page<SettlementRecord> findByStoreIdAndSettlementDateBetweenOrderBySettlementDateDesc(
        Long storeId,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    );
}

