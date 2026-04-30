package com.dukaanpe.payment.repository;

import com.dukaanpe.payment.entity.IdempotencyRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKeyAndOperation(String idempotencyKey, String operation);
}

