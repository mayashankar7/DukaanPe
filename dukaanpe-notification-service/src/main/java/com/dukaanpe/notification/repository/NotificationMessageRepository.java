package com.dukaanpe.notification.repository;

import com.dukaanpe.notification.entity.NotificationMessageEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationMessageRepository extends JpaRepository<NotificationMessageEntity, Long> {

    List<NotificationMessageEntity> findByStoreIdOrderByIdDesc(Long storeId);

    long countByStatusIn(Collection<String> statuses);

    Optional<NotificationMessageEntity> findFirstByProviderMessageId(String providerMessageId);

    List<NotificationMessageEntity> findTop100ByStatusAndNextRetryAtBeforeOrderByNextRetryAtAsc(
        String status,
        LocalDateTime threshold
    );
}

