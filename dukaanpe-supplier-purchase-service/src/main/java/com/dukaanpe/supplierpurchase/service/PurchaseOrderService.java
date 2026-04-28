package com.dukaanpe.supplierpurchase.service;

import com.dukaanpe.supplierpurchase.dto.PagedResponse;
import com.dukaanpe.supplierpurchase.dto.AutoReorderSuggestionResponse;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderRequest;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderResponse;
import com.dukaanpe.supplierpurchase.dto.UpdatePurchaseOrderStatusRequest;
import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);

    PagedResponse<PurchaseOrderResponse> listPurchaseOrders(Long storeId, int page, int size);

    PurchaseOrderResponse getPurchaseOrder(Long id);

    PurchaseOrderResponse updatePurchaseOrder(Long id, PurchaseOrderRequest request);

    PurchaseOrderResponse updateStatus(Long id, UpdatePurchaseOrderStatusRequest request);

    void cancelPurchaseOrder(Long id);

    List<AutoReorderSuggestionResponse> autoSuggest(Long storeId, int limit);
}

