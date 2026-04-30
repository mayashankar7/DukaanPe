package com.dukaanpe.payment.repository;

import com.dukaanpe.payment.entity.DailyCashRegister;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyCashRegisterRepository extends JpaRepository<DailyCashRegister, Long> {

    Optional<DailyCashRegister> findTopByStoreIdAndTerminalIdAndIsClosedFalseOrderByRegisterDateDesc(
        Long storeId,
        String terminalId
    );

    boolean existsByStoreIdAndTerminalIdAndRegisterDateAndIsClosedFalse(Long storeId, String terminalId, LocalDate registerDate);
}

