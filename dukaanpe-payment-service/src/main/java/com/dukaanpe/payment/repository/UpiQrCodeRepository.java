package com.dukaanpe.payment.repository;

import com.dukaanpe.payment.entity.UpiQrCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpiQrCodeRepository extends JpaRepository<UpiQrCode, Long> {

    List<UpiQrCode> findByStoreIdOrderByCreatedAtDesc(Long storeId);
}

