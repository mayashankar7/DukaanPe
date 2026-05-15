package com.dukaanpe.syncoffline.controller;

import com.dukaanpe.syncoffline.dto.ApiResponse;
import com.dukaanpe.syncoffline.dto.SyncEventResponse;
import com.dukaanpe.syncoffline.dto.SyncPushRequest;
import com.dukaanpe.syncoffline.service.SyncService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@Validated
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/push")
    public ResponseEntity<ApiResponse<SyncEventResponse>> push(@Valid @RequestBody SyncPushRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Sync event accepted", syncService.push(request)));
    }

    @GetMapping("/pull")
    public ResponseEntity<ApiResponse<List<SyncEventResponse>>> pull(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since
    ) {
        return ResponseEntity.ok(ApiResponse.success(syncService.pull(storeId, since)));
    }
}

