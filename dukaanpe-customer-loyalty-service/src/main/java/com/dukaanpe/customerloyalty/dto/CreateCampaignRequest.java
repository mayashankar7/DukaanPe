package com.dukaanpe.customerloyalty.dto;

import com.dukaanpe.customerloyalty.entity.CampaignType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateCampaignRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotBlank(message = "campaignName is required")
    @Size(max = 160, message = "campaignName must be <= 160 chars")
    private String campaignName;

    @NotNull(message = "campaignType is required")
    private CampaignType campaignType;

    @Size(max = 500, message = "description must be <= 500 chars")
    private String description;

    private BigDecimal discountPercent;

    private BigDecimal discountAmount;

    private BigDecimal minPurchaseAmount;

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    @NotBlank(message = "targetTier is required")
    @Size(max = 120, message = "targetTier must be <= 120 chars")
    private String targetTier;

    @Size(max = 500, message = "messageTemplate must be <= 500 chars")
    private String messageTemplate;
}

