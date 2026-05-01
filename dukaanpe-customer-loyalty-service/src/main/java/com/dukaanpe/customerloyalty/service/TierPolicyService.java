package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.entity.CustomerTier;
import java.math.BigDecimal;

public interface TierPolicyService {

    CustomerTier resolveTier(Long storeId, BigDecimal totalPurchases);
}

