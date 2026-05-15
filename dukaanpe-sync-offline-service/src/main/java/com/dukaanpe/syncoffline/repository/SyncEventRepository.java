package com.dukaanpe.syncoffline.repository;

import com.dukaanpe.syncoffline.entity.SyncEventEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncEventRepository extends JpaRepository<SyncEventEntity, Long> {

    Optional<SyncEventEntity> findTopByDedupeKeyAndCreatedAtAfterOrderByCreatedAtDesc(String dedupeKey, LocalDateTime threshold);

    Optional<SyncEventEntity> findTopByStoreIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(Long storeId, String entityType, String entityId);

    List<SyncEventEntity> findByStoreIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(Long storeId, LocalDateTime since);
}

