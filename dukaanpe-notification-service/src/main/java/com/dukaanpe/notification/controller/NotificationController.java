package com.dukaanpe.notification.controller;

import com.dukaanpe.notification.dto.ApiResponse;
import com.dukaanpe.notification.dto.NotificationCallbackRequest;
import com.dukaanpe.notification.dto.NotificationRequest;
import com.dukaanpe.notification.dto.NotificationResponse;
import com.dukaanpe.notification.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Notification queued", notificationService.send(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.listByStore(storeId)));
    }

    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<Void>> callback(@Valid @RequestBody NotificationCallbackRequest request) {
        notificationService.callback(request);
        return ResponseEntity.ok(ApiResponse.success("Callback accepted", null));
    }
}

