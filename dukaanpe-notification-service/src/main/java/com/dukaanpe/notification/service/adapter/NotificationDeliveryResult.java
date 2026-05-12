package com.dukaanpe.notification.service.adapter;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationDeliveryResult {

    String status;
    String providerMessageId;
    String failureReason;

    public static NotificationDeliveryResult sent(String providerMessageId) {
        return NotificationDeliveryResult.builder()
            .status("SENT")
            .providerMessageId(providerMessageId)
            .build();
    }

    public static NotificationDeliveryResult failed(String reason) {
        return NotificationDeliveryResult.builder()
            .status("FAILED")
            .failureReason(reason)
            .build();
    }
}

