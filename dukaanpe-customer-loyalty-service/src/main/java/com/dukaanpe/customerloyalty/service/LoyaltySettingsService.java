package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.dto.LoyaltySettingsRequest;
import com.dukaanpe.customerloyalty.dto.LoyaltySettingsResponse;
import com.dukaanpe.customerloyalty.entity.LoyaltySettings;

public interface LoyaltySettingsService {

    LoyaltySettingsResponse getSettings(Long storeId);

    LoyaltySettingsResponse upsertSettings(LoyaltySettingsRequest request);

    LoyaltySettings getOrCreateSettingsEntity(Long storeId);
}

