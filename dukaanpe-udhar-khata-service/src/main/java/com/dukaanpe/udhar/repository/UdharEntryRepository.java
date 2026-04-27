package com.dukaanpe.udhar.repository;

import com.dukaanpe.udhar.entity.UdharEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UdharEntryRepository extends JpaRepository<UdharEntry, Long> {

    List<UdharEntry> findByKhataCustomerIdOrderByCreatedAtDesc(Long khataCustomerId);

    List<UdharEntry> findByStoreIdAndDueDateBeforeAndRunningBalanceGreaterThanOrderByDueDateAsc(
        Long storeId,
        LocalDate dueDate,
        BigDecimal runningBalance
    );

    Page<UdharEntry> findByKhataCustomerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long khataCustomerId,
        java.time.LocalDateTime fromDateTime,
        java.time.LocalDateTime toDateTime,
        Pageable pageable
    );

    Page<UdharEntry> findByStoreIdAndDueDateBetweenAndRunningBalanceGreaterThanOrderByDueDateAsc(
        Long storeId,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal runningBalance,
        Pageable pageable
    );
}

