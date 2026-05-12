package com.dukaanpe.notification.service;

import com.dukaanpe.notification.dto.NotificationCallbackRequest;
import com.dukaanpe.notification.dto.NotificationRequest;
import com.dukaanpe.notification.dto.NotificationResponse;
import com.dukaanpe.notification.entity.NotificationMessageEntity;
import com.dukaanpe.notification.repository.NotificationMessageRepository;
import com.dukaanpe.notification.service.adapter.NotificationChannelAdapter;
import com.dukaanpe.notification.service.adapter.NotificationDeliveryResult;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InMemoryNotificationService implements NotificationService {

    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_RETRYING = "RETRYING";

    private final NotificationMessageRepository repository;
    private final List<NotificationChannelAdapter> channelAdapters;
    @Value("${notification.max-attempts:3}")
    private int maxAttempts;
    @Value("${notification.max-queue-size:5000}")
    private long maxQueueSize;
    @Value("${notification.retry-backoff-seconds:30}")
    private long retryBackoffSeconds;

    @Override
    public NotificationResponse send(NotificationRequest request) {
        long inFlight = repository.countByStatusIn(List.of("QUEUED", STATUS_RETRYING));
        if (inFlight >= maxQueueSize) {
            throw new IllegalStateException("Notification queue is full. Try again shortly.");
        }

        String normalizedChannel = request.getChannel().trim().toUpperCase(Locale.ROOT);
        NotificationMessageEntity entity = NotificationMessageEntity.builder()
            .storeId(request.getStoreId())
            .channel(normalizedChannel)
            .recipient(request.getRecipient().trim())
            .title(request.getTitle().trim())
            .message(request.getMessage().trim())
            .status("QUEUED")
            .attemptCount(0)
            .lastError(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        entity = repository.save(entity);
        NotificationMessageEntity processed = processDelivery(entity);
        return toResponse(processed);
    }

    @Override
    public List<NotificationResponse> listByStore(Long storeId) {
        return repository.findByStoreIdOrderByIdDesc(storeId).stream()
            .map(this::toResponse)
            .sorted(Comparator.comparing(NotificationResponse::getId).reversed())
            .toList();
    }

    @Override
    public void callback(NotificationCallbackRequest request) {
        NotificationMessageEntity entity = repository.findFirstByProviderMessageId(request.getProviderMessageId())
            .orElseThrow(() -> new IllegalArgumentException("Unknown providerMessageId: " + request.getProviderMessageId()));

        String normalizedStatus = request.getStatus().trim().toUpperCase(Locale.ROOT);
        entity.setStatus(normalizedStatus);
        entity.setLastError(request.getFailureReason());
        entity.setUpdatedAt(LocalDateTime.now());
        if (STATUS_SENT.equals(normalizedStatus)) {
            entity.setSentAt(LocalDateTime.now());
        }
        repository.save(entity);
    }

    @Scheduled(fixedDelayString = "${notification.retry-scheduler-delay-ms:15000}")
    public void retryPending() {
        List<NotificationMessageEntity> pending = repository.findTop100ByStatusAndNextRetryAtBeforeOrderByNextRetryAtAsc(
            STATUS_RETRYING,
            LocalDateTime.now()
        );
        for (NotificationMessageEntity row : pending) {
            processDelivery(row);
        }
    }

    private NotificationMessageEntity processDelivery(NotificationMessageEntity entity) {
        NotificationRequest request = new NotificationRequest();
        request.setStoreId(entity.getStoreId());
        request.setChannel(entity.getChannel());
        request.setRecipient(entity.getRecipient());
        request.setTitle(entity.getTitle());
        request.setMessage(entity.getMessage());

        NotificationDeliveryResult deliveryResult = channelAdapters.stream()
            .filter(adapter -> adapter.supports(entity.getChannel()))
            .findFirst()
            .map(adapter -> adapter.deliver(request))
            .orElse(NotificationDeliveryResult.failed("Unsupported channel: " + entity.getChannel()));

        entity.setAttemptCount(entity.getAttemptCount() + 1);
        entity.setProviderMessageId(deliveryResult.getProviderMessageId());
        entity.setUpdatedAt(LocalDateTime.now());

        if (STATUS_SENT.equalsIgnoreCase(deliveryResult.getStatus())) {
            entity.setStatus(STATUS_SENT);
            entity.setSentAt(LocalDateTime.now());
            entity.setLastError(null);
            entity.setNextRetryAt(null);
        } else {
            entity.setLastError(deliveryResult.getFailureReason());
            boolean isUnsupported = deliveryResult.getFailureReason() != null
                && deliveryResult.getFailureReason().toLowerCase(Locale.ROOT).contains("unsupported channel");
            if (isUnsupported || entity.getAttemptCount() >= maxAttempts) {
                entity.setStatus(STATUS_FAILED);
                entity.setNextRetryAt(null);
                log.warn("Notification delivery failed permanently id={} reason={}", entity.getId(), deliveryResult.getFailureReason());
            } else {
                entity.setStatus(STATUS_RETRYING);
                entity.setNextRetryAt(LocalDateTime.now().plusSeconds(retryBackoffSeconds));
                log.info("Notification scheduled for retry id={} attempt={} nextRetryAt={}", entity.getId(), entity.getAttemptCount(), entity.getNextRetryAt());
            }
        }

        return repository.save(entity);
    }

    private NotificationResponse toResponse(NotificationMessageEntity entity) {
        return NotificationResponse.builder()
            .id(entity.getId())
            .storeId(entity.getStoreId())
            .channel(entity.getChannel())
            .recipient(entity.getRecipient())
            .title(entity.getTitle())
            .message(entity.getMessage())
            .status(entity.getStatus())
            .sentAt(entity.getSentAt())
            .build();
    }
}

