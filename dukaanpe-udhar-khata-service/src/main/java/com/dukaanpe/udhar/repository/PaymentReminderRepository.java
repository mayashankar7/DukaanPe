package com.dukaanpe.udhar.repository;

import com.dukaanpe.udhar.entity.PaymentReminder;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {

    Page<PaymentReminder> findByStoreIdAndIsSentFalseAndReminderDateBetweenOrderByReminderDateAsc(
        Long storeId,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    );

    Page<PaymentReminder> findByKhataCustomerIdAndReminderDateBetweenOrderByCreatedAtDesc(
        Long customerId,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    );
}

