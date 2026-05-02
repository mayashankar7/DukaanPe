package com.dukaanpe.gsttax.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class HsnResponse {

    private Long id;
    private String hsnCode;
    private String description;
    private BigDecimal gstRate;
    private BigDecimal cessRate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

