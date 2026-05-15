package com.dukaanpe.syncoffline.service;

import com.dukaanpe.syncoffline.dto.SyncEventResponse;
import com.dukaanpe.syncoffline.dto.SyncPushRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface SyncService {

    SyncEventResponse push(SyncPushRequest request);

    List<SyncEventResponse> pull(Long storeId, LocalDateTime since);
}

