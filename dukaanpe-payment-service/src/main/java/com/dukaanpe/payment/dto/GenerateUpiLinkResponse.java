package com.dukaanpe.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateUpiLinkResponse {

    private String upiLink;
}

