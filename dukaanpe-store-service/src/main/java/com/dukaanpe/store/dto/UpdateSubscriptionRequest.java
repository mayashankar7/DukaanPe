package com.dukaanpe.store.dto;

import com.dukaanpe.store.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSubscriptionRequest {

    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan subscriptionPlan;
}

