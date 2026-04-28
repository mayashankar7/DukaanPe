package com.dukaanpe.supplierpurchase.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoReorderSuggestionResponse {

    private Long supplierId;
    private String supplierName;
    private int totalItems;
    private List<AutoReorderSuggestionItemResponse> items;
}

