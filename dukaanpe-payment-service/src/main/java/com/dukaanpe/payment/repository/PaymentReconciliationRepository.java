package com.dukaanpe.payment.repository;

import com.dukaanpe.payment.entity.PaymentReconciliation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentReconciliationRepository extends JpaRepository<PaymentReconciliation, Long> {

    List<PaymentReconciliation> findByStoreIdAndReconDateBetweenOrderByReconDateDesc(
        Long storeId,
        LocalDate from,
        LocalDate to
    );

    boolean existsByStoreIdAndReconDate(Long storeId, LocalDate reconDate);

    Optional<PaymentReconciliation> findByStoreIdAndReconDate(Long storeId, LocalDate reconDate);
}

