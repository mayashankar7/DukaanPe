package com.dukaanpe.store.dto;

import com.dukaanpe.store.entity.BusinessCategory;
import com.dukaanpe.store.entity.SubscriptionPlan;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private Long id;
    private String ownerPhone;
    private String storeName;
    private String storeNameRegional;
    private BusinessCategory businessCategory;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    private String gstin;
    private String fssaiLicense;
    private String drugLicense;
    private String tradeLicense;
    private Double latitude;
    private Double longitude;
    private String logoUrl;
    private Boolean isActive;
    private SubscriptionPlan subscriptionPlan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

