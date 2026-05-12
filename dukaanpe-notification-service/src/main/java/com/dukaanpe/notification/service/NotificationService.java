package com.dukaanpe.notification.service;

import com.dukaanpe.notification.dto.NotificationCallbackRequest;
import com.dukaanpe.notification.dto.NotificationRequest;
import com.dukaanpe.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse send(NotificationRequest request);

    List<NotificationResponse> listByStore(Long storeId);

    void callback(NotificationCallbackRequest request);
}

