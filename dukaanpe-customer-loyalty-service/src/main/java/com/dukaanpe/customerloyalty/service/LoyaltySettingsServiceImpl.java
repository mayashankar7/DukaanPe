package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.dto.LoyaltySettingsRequest;
import com.dukaanpe.customerloyalty.dto.LoyaltySettingsResponse;
import com.dukaanpe.customerloyalty.entity.LoyaltySettings;
import com.dukaanpe.customerloyalty.exception.InvalidLoyaltyOperationException;
import com.dukaanpe.customerloyalty.repository.LoyaltySettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoyaltySettingsServiceImpl implements LoyaltySettingsService {

    private final LoyaltySettingsRepository loyaltySettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public LoyaltySettingsResponse getSettings(Long storeId) {
        return toResponse(getOrCreateSettingsEntity(storeId));
    }

    @Override
    public LoyaltySettingsResponse upsertSettings(LoyaltySettingsRequest request) {
        validateThresholds(request);
        LoyaltySettings settings = loyaltySettingsRepository.findByStoreId(request.getStoreId())
            .orElse(LoyaltySettings.builder().storeId(request.getStoreId()).build());

        settings.setPointsPerHundred(request.getPointsPerHundred());
        settings.setPointsToRedeemUnit(request.getPointsToRedeemUnit());
        settings.setRedeemValueRupees(request.getRedeemValueRupees());
        settings.setSilverThreshold(request.getSilverThreshold());
        settings.setGoldThreshold(request.getGoldThreshold());
        settings.setPlatinumThreshold(request.getPlatinumThreshold());

        return toResponse(loyaltySettingsRepository.save(settings));
    }

    @Override
    public LoyaltySettings getOrCreateSettingsEntity(Long storeId) {
        return loyaltySettingsRepository.findByStoreId(storeId)
            .orElseGet(() -> loyaltySettingsRepository.save(LoyaltySettings.builder().storeId(storeId).build()));
    }

    private void validateThresholds(LoyaltySettingsRequest request) {
        if (request.getSilverThreshold().compareTo(request.getGoldThreshold()) > 0
            || request.getGoldThreshold().compareTo(request.getPlatinumThreshold()) > 0) {
            throw new InvalidLoyaltyOperationException("Thresholds must satisfy silver <= gold <= platinum");
        }
    }

    private LoyaltySettingsResponse toResponse(LoyaltySettings entity) {
        return LoyaltySettingsResponse.builder()
            .id(entity.getId())
            .storeId(entity.getStoreId())
            .pointsPerHundred(entity.getPointsPerHundred())
            .pointsToRedeemUnit(entity.getPointsToRedeemUnit())
            .redeemValueRupees(entity.getRedeemValueRupees())
            .silverThreshold(entity.getSilverThreshold())
            .goldThreshold(entity.getGoldThreshold())
            .platinumThreshold(entity.getPlatinumThreshold())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}

