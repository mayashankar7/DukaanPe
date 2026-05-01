package com.dukaanpe.customerloyalty.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EligibleCustomerCountResponse {

    private long count;
}

