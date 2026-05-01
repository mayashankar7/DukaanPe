package com.dukaanpe.customerloyalty.dto;

import com.dukaanpe.customerloyalty.entity.CampaignType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignResponse {

    private Long id;
    private Long storeId;
    private String campaignName;
    private CampaignType campaignType;
    private String description;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal minPurchaseAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String targetTier;
    private String messageTemplate;
    private Boolean isActive;
    private Integer totalCustomersTargeted;
    private Integer totalRedemptions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

