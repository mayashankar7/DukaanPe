package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.dto.EarnLoyaltyPointsRequest;
import com.dukaanpe.customerloyalty.dto.LoyaltySummaryResponse;
import com.dukaanpe.customerloyalty.dto.LoyaltyTransactionResponse;
import com.dukaanpe.customerloyalty.dto.PagedResponse;
import com.dukaanpe.customerloyalty.dto.RedeemLoyaltyPointsRequest;

public interface LoyaltyService {

    LoyaltySummaryResponse getLoyaltySummary(Long customerId);

    LoyaltyTransactionResponse earnPoints(EarnLoyaltyPointsRequest request);

    LoyaltyTransactionResponse redeemPoints(RedeemLoyaltyPointsRequest request);

    PagedResponse<LoyaltyTransactionResponse> getTransactions(Long customerId, int page, int size);
}

