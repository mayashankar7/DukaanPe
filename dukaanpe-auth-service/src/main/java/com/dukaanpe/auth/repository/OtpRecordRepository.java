package com.dukaanpe.auth.repository;

import com.dukaanpe.auth.entity.OtpRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRecordRepository extends JpaRepository<OtpRecord, Long> {

    Optional<OtpRecord> findTopByPhoneNumberAndIsUsedFalseOrderByCreatedAtDesc(String phoneNumber);
}

