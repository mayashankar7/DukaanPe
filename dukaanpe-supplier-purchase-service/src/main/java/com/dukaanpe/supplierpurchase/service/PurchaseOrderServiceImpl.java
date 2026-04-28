package com.dukaanpe.supplierpurchase.service;

import com.dukaanpe.supplierpurchase.dto.AutoReorderSuggestionItemResponse;
import com.dukaanpe.supplierpurchase.dto.AutoReorderSuggestionResponse;
import com.dukaanpe.supplierpurchase.dto.PagedResponse;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderItemRequest;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderItemResponse;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderRequest;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderResponse;
import com.dukaanpe.supplierpurchase.dto.UpdatePurchaseOrderStatusRequest;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrder;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrderItem;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrderStatus;
import com.dukaanpe.supplierpurchase.entity.Supplier;
import com.dukaanpe.supplierpurchase.exception.ResourceNotFoundException;
import com.dukaanpe.supplierpurchase.repository.PurchaseOrderRepository;
import com.dukaanpe.supplierpurchase.repository.SupplierRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final RestTemplate restTemplate;

    @Value("${supplier.auto-suggest.use-inventory:true}")
    private boolean useInventoryForSuggestions;

    @Value("${supplier.auto-suggest.inventory-base-url:http://localhost:8083}")
    private String inventoryBaseUrl;

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        validateSupplier(request.getSupplierId(), request.getStoreId());
        PurchaseOrder order = new PurchaseOrder();
        mapOrder(order, request);
        order.setPoNumber(generatePoNumber(request.getOrderDate()));
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PurchaseOrderResponse> listPurchaseOrders(Long storeId, int page, int size) {
        Page<PurchaseOrder> result = purchaseOrderRepository.findByStoreIdOrderByOrderDateDescIdDesc(
            storeId,
            PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))
        );
        return PagedResponse.<PurchaseOrderResponse>builder()
            .content(result.getContent().stream().map(this::toResponse).toList())
            .pageNumber(result.getNumber())
            .pageSize(result.getSize())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .last(result.isLast())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrder(Long id) {
        return toResponse(getOrder(id));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse updatePurchaseOrder(Long id, PurchaseOrderRequest request) {
        validateSupplier(request.getSupplierId(), request.getStoreId());
        PurchaseOrder order = getOrder(id);
        mapOrder(order, request);
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse updateStatus(Long id, UpdatePurchaseOrderStatusRequest request) {
        PurchaseOrder order = getOrder(id);
        order.setStatus(request.getStatus());
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancelPurchaseOrder(Long id) {
        PurchaseOrder order = getOrder(id);
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutoReorderSuggestionResponse> autoSuggest(Long storeId, int limit) {
        int cappedLimit = Math.min(Math.max(limit, 1), 50);
        List<AutoReorderSuggestionResponse> suggestions = useInventoryForSuggestions
            ? suggestionsFromInventory(storeId, cappedLimit)
            : List.of();
        if (!suggestions.isEmpty()) {
            return suggestions;
        }
        return suggestionsFromHistory(storeId, cappedLimit);
    }

    private PurchaseOrder getOrder(Long id) {
        return purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    private List<AutoReorderSuggestionResponse> suggestionsFromInventory(Long storeId, int limit) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                inventoryBaseUrl + "/api/inventory/low-stock?storeId={storeId}",
                Map.class,
                storeId
            );
            if (response == null || !(response.get("data") instanceof List<?> items)) {
                return List.of();
            }

            Map<Long, AutoReorderSuggestionResponse> grouped = new HashMap<>();
            for (Object obj : items) {
                if (!(obj instanceof Map<?, ?> raw)) {
                    continue;
                }
                String productName = asString(raw.get("productName"));
                if (productName == null || productName.isBlank()) {
                    continue;
                }

                Supplier supplier = resolveSupplierForProduct(storeId, productName).orElse(null);
                if (supplier == null) {
                    continue;
                }

                Double current = asDouble(raw.get("currentStock"));
                Double reorder = asDouble(raw.get("reorderLevel"));
                Double max = asDouble(raw.get("maxStockLevel"));
                double suggested = computeSuggested(current, reorder, max);

                AutoReorderSuggestionItemResponse item = AutoReorderSuggestionItemResponse.builder()
                    .productId(asLong(raw.get("productId")))
                    .productName(productName)
                    .currentStock(current)
                    .reorderLevel(reorder)
                    .suggestedQuantity(suggested)
                    .build();

                grouped.computeIfAbsent(supplier.getId(), key -> AutoReorderSuggestionResponse.builder()
                        .supplierId(supplier.getId())
                        .supplierName(supplier.getSupplierName())
                        .items(new ArrayList<>())
                        .totalItems(0)
                        .build())
                    .getItems()
                    .add(item);
            }
            return grouped.values().stream()
                .peek(group -> group.setTotalItems(group.getItems().size()))
                .limit(limit)
                .toList();
        } catch (Exception ex) {
            log.warn("Inventory-driven auto-suggest failed for store {}: {}", storeId, ex.getMessage());
            return List.of();
        }
    }

    private List<AutoReorderSuggestionResponse> suggestionsFromHistory(Long storeId, int limit) {
        List<PurchaseOrder> recent = purchaseOrderRepository.findTop20ByStoreIdOrderByOrderDateDescIdDesc(storeId);
        Map<Long, AutoReorderSuggestionResponse> grouped = new HashMap<>();
        for (PurchaseOrder order : recent) {
            Supplier supplier = supplierRepository.findById(order.getSupplierId())
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .orElse(null);
            if (supplier == null) {
                continue;
            }

            for (PurchaseOrderItem item : order.getItems()) {
                double suggested = Math.max(1D, item.getQuantityOrdered() - item.getQuantityReceived());
                AutoReorderSuggestionItemResponse suggestionItem = AutoReorderSuggestionItemResponse.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .currentStock(null)
                    .reorderLevel(null)
                    .suggestedQuantity(suggested)
                    .build();

                grouped.computeIfAbsent(supplier.getId(), key -> AutoReorderSuggestionResponse.builder()
                        .supplierId(supplier.getId())
                        .supplierName(supplier.getSupplierName())
                        .items(new ArrayList<>())
                        .totalItems(0)
                        .build())
                    .getItems()
                    .add(suggestionItem);
            }
        }

        return grouped.values().stream()
            .peek(group -> {
                if (group.getItems().size() > limit) {
                    group.setItems(group.getItems().subList(0, limit));
                }
                group.setTotalItems(group.getItems().size());
            })
            .limit(limit)
            .toList();
    }

    private Optional<Supplier> resolveSupplierForProduct(Long storeId, String productName) {
        List<PurchaseOrder> matches = purchaseOrderRepository.findRecentByStoreAndProductName(
            storeId,
            productName,
            PageRequest.of(0, 1)
        );
        if (!matches.isEmpty()) {
            return supplierRepository.findById(matches.get(0).getSupplierId())
                .filter(supplier -> Boolean.TRUE.equals(supplier.getIsActive()));
        }
        return supplierRepository.findFirstByStoreIdAndIsActiveTrueOrderBySupplierNameAsc(storeId);
    }

    private double computeSuggested(Double currentStock, Double reorderLevel, Double maxStockLevel) {
        double current = currentStock == null ? 0D : currentStock;
        double reorder = reorderLevel == null ? 0D : reorderLevel;
        double max = maxStockLevel == null ? Math.max(reorder * 2, current + 10D) : maxStockLevel;
        return Math.max(1D, max - current);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private void validateSupplier(Long supplierId, Long storeId) {
        supplierRepository.findById(supplierId)
            .filter(supplier -> Boolean.TRUE.equals(supplier.getIsActive()) && supplier.getStoreId().equals(storeId))
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found for storeId=" + storeId + " supplierId=" + supplierId));
    }

    private void mapOrder(PurchaseOrder order, PurchaseOrderRequest request) {
        order.setStoreId(request.getStoreId());
        order.setSupplierId(request.getSupplierId());
        order.setOrderDate(request.getOrderDate());
        order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        order.setStatus(request.getStatus() == null ? PurchaseOrderStatus.DRAFT : request.getStatus());
        order.setNotes(request.getNotes());
        order.setCreatedBy(request.getCreatedBy());

        List<PurchaseOrderItem> items = request.getItems().stream().map(item -> toItem(order, item)).toList();
        order.getItems().clear();
        order.getItems().addAll(items);

        BigDecimal subtotal = items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantityOrdered())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gstAmount = items.stream()
            .map(item -> item.getTotalAmount().subtract(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantityOrdered()))))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(scale(subtotal));
        order.setGstAmount(scale(gstAmount));
        order.setTotalAmount(scale(subtotal.add(gstAmount)));
    }

    private PurchaseOrderItem toItem(PurchaseOrder order, PurchaseOrderItemRequest item) {
        BigDecimal baseAmount = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantityOrdered()));
        BigDecimal gstAmount = baseAmount.multiply(item.getGstRate()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return PurchaseOrderItem.builder()
            .purchaseOrder(order)
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantityOrdered(item.getQuantityOrdered())
            .quantityReceived(0.0)
            .unit(item.getUnit())
            .unitPrice(scale(item.getUnitPrice()))
            .gstRate(scale(item.getGstRate()))
            .totalAmount(scale(baseAmount.add(gstAmount)))
            .build();
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder order) {
        return PurchaseOrderResponse.builder()
            .id(order.getId())
            .storeId(order.getStoreId())
            .supplierId(order.getSupplierId())
            .poNumber(order.getPoNumber())
            .orderDate(order.getOrderDate())
            .expectedDeliveryDate(order.getExpectedDeliveryDate())
            .status(order.getStatus())
            .subtotal(order.getSubtotal())
            .gstAmount(order.getGstAmount())
            .totalAmount(order.getTotalAmount())
            .notes(order.getNotes())
            .createdBy(order.getCreatedBy())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(order.getItems().stream().map(this::toItemResponse).toList())
            .build();
    }

    private PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        return PurchaseOrderItemResponse.builder()
            .id(item.getId())
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantityOrdered(item.getQuantityOrdered())
            .quantityReceived(item.getQuantityReceived())
            .unit(item.getUnit())
            .unitPrice(item.getUnitPrice())
            .gstRate(item.getGstRate())
            .totalAmount(item.getTotalAmount())
            .build();
    }

    private String generatePoNumber(LocalDate orderDate) {
        long count = purchaseOrderRepository.countByOrderDate(orderDate) + 1;
        return String.format("PO-%s-%03d", DATE_FORMATTER.format(orderDate), count);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

