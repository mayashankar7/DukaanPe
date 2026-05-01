package com.dukaanpe.customerloyalty.dto;

import com.dukaanpe.customerloyalty.entity.CustomerTier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {

    private Long id;
    private Long storeId;
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate anniversaryDate;
    private BigDecimal totalPurchases;
    private Integer totalVisits;
    private LocalDate lastVisitDate;
    private Integer loyaltyPoints;
    private CustomerTier customerTier;
    private String tags;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

