package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.entity.CustomerTier;
import com.dukaanpe.customerloyalty.entity.LoyaltySettings;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TierPolicyServiceImpl implements TierPolicyService {

    private final LoyaltySettingsService loyaltySettingsService;

    @Override
    public CustomerTier resolveTier(Long storeId, BigDecimal totalPurchases) {
        LoyaltySettings settings = loyaltySettingsService.getOrCreateSettingsEntity(storeId);
        if (totalPurchases.compareTo(settings.getPlatinumThreshold()) >= 0) {
            return CustomerTier.PLATINUM;
        }
        if (totalPurchases.compareTo(settings.getGoldThreshold()) >= 0) {
            return CustomerTier.GOLD;
        }
        if (totalPurchases.compareTo(settings.getSilverThreshold()) >= 0) {
            return CustomerTier.SILVER;
        }
        return CustomerTier.REGULAR;
    }
}

