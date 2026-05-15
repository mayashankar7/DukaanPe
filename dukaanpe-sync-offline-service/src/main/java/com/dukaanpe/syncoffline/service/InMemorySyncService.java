package com.dukaanpe.syncoffline.service;

import com.dukaanpe.syncoffline.dto.SyncEventResponse;
import com.dukaanpe.syncoffline.dto.SyncPushRequest;
import com.dukaanpe.syncoffline.entity.SyncEventEntity;
import com.dukaanpe.syncoffline.repository.SyncEventRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Slf4j
public class InMemorySyncService implements SyncService {

    private static final Set<String> SUPPORTED_OPERATIONS = Set.of("CREATE", "UPDATE", "DELETE", "UPSERT");
    private final SyncEventRepository repository;
    @Value("${sync.dedupe-window-seconds:5}")
    private long dedupeWindowSeconds;
    @Value("${sync.conflict-window-seconds:300}")
    private long conflictWindowSeconds;

    @Override
    public SyncEventResponse push(SyncPushRequest request) {
        String normalizedOperation = request.getOperation().trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_OPERATIONS.contains(normalizedOperation)) {
            throw new IllegalArgumentException("operation must be one of " + SUPPORTED_OPERATIONS);
        }

        LocalDateTime now = LocalDateTime.now();
        String normalizedEntityType = request.getEntityType().trim().toUpperCase(Locale.ROOT);
        String normalizedEntityId = request.getEntityId().trim();
        String dedupeKey = request.getStoreId() + "|" + normalizedEntityType
            + "|" + request.getEntityId().trim() + "|" + normalizedOperation + "|" + request.getPayload().trim();

        var dedupeHit = repository.findTopByDedupeKeyAndCreatedAtAfterOrderByCreatedAtDesc(
            dedupeKey,
            now.minusSeconds(dedupeWindowSeconds)
        );
        if (dedupeHit.isPresent()) {
            return toResponse(dedupeHit.get());
        }

        var latestEvent = repository.findTopByStoreIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            request.getStoreId(),
            normalizedEntityType,
            normalizedEntityId
        );
        boolean conflictDetected = false;
        String conflictReason = null;
        if (latestEvent.isPresent()) {
            SyncEventEntity previous = latestEvent.get();
            boolean withinWindow = !previous.getCreatedAt().isBefore(now.minusSeconds(conflictWindowSeconds));
            boolean payloadChanged = !previous.getPayload().equals(request.getPayload().trim());
            if (withinWindow && payloadChanged) {
                conflictDetected = true;
                conflictReason = "Payload changed within conflict window";
                log.warn(
                    "Sync conflict candidate storeId={} entityType={} entityId={} previousId={}",
                    request.getStoreId(),
                    normalizedEntityType,
                    normalizedEntityId,
                    previous.getId()
                );
            }
        }

        SyncEventEntity saved = repository.save(SyncEventEntity.builder()
            .storeId(request.getStoreId())
            .entityType(normalizedEntityType)
            .entityId(normalizedEntityId)
            .operation(normalizedOperation)
            .payload(request.getPayload().trim())
            .dedupeKey(dedupeKey)
            .conflictDetected(conflictDetected)
            .conflictReason(conflictReason)
            .createdAt(now)
            .build());
        return toResponse(saved);
    }

    @Override
    public List<SyncEventResponse> pull(Long storeId, LocalDateTime since) {
        return repository.findByStoreIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(storeId, since).stream()
            .map(this::toResponse)
            .toList();
    }

    private SyncEventResponse toResponse(SyncEventEntity entity) {
        return SyncEventResponse.builder()
            .id(entity.getId())
            .storeId(entity.getStoreId())
            .entityType(entity.getEntityType())
            .entityId(entity.getEntityId())
            .operation(entity.getOperation())
            .payload(entity.getPayload())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}

