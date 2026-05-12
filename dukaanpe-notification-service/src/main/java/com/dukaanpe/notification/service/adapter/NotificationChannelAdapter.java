package com.dukaanpe.notification.service.adapter;

import com.dukaanpe.notification.dto.NotificationRequest;

public interface NotificationChannelAdapter {

    boolean supports(String normalizedChannel);

    NotificationDeliveryResult deliver(NotificationRequest request);
}

