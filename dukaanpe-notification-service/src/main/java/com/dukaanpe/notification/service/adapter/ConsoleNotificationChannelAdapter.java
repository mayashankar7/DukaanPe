package com.dukaanpe.notification.service.adapter;

import com.dukaanpe.notification.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class ConsoleNotificationChannelAdapter implements NotificationChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationChannelAdapter.class);
    private static final Set<String> SUPPORTED_CHANNELS = Set.of("SMS", "WHATSAPP", "PUSH", "EMAIL");

    @Override
    public boolean supports(String normalizedChannel) {
        return SUPPORTED_CHANNELS.contains(normalizedChannel);
    }

    @Override
    public NotificationDeliveryResult deliver(NotificationRequest request) {
        String channel = request.getChannel().trim().toUpperCase();
        String providerId = UUID.randomUUID().toString();
        log.info("Notification adapter send channel={} recipient={} title={} providerId={}", channel, request.getRecipient(), request.getTitle(), providerId);
        return NotificationDeliveryResult.sent(providerId);
    }
}

