package com.dukaanpe.payment.repository;

import com.dukaanpe.payment.entity.PaymentMode;
import com.dukaanpe.payment.entity.PaymentStatus;
import com.dukaanpe.payment.entity.PaymentTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByTransactionId(String transactionId);

    Page<PaymentTransaction> findByStoreId(Long storeId, Pageable pageable);

    @Query("select pt from PaymentTransaction pt where pt.storeId = :storeId and pt.createdAt >= :from and pt.createdAt < :to")
    Page<PaymentTransaction> findByStoreIdAndCreatedAtBetween(
        @Param("storeId") Long storeId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );

    @Query("select coalesce(sum(pt.amount), 0) from PaymentTransaction pt "
        + "where pt.storeId = :storeId and pt.createdAt >= :from and pt.createdAt < :to "
        + "and pt.paymentMode = :paymentMode and pt.paymentStatus = :paymentStatus "
        + "and (:terminalId is null or pt.terminalId = :terminalId)")
    BigDecimal sumAmountByStoreAndDateAndModeAndStatusAndTerminal(
        @Param("storeId") Long storeId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        @Param("paymentMode") PaymentMode paymentMode,
        @Param("paymentStatus") PaymentStatus paymentStatus,
        @Param("terminalId") String terminalId
    );
}

