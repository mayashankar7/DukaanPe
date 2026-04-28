package com.dukaanpe.supplierpurchase.service;

import com.dukaanpe.supplierpurchase.dto.GrnItemRequest;
import com.dukaanpe.supplierpurchase.dto.GrnItemResponse;
import com.dukaanpe.supplierpurchase.dto.GrnRequest;
import com.dukaanpe.supplierpurchase.dto.GrnResponse;
import com.dukaanpe.supplierpurchase.dto.InventoryAdjustHookRequest;
import com.dukaanpe.supplierpurchase.entity.GoodsReceivedNote;
import com.dukaanpe.supplierpurchase.entity.GrnItem;
import com.dukaanpe.supplierpurchase.entity.GrnStatus;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrder;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrderItem;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrderStatus;
import com.dukaanpe.supplierpurchase.exception.ResourceNotFoundException;
import com.dukaanpe.supplierpurchase.repository.GoodsReceivedNoteRepository;
import com.dukaanpe.supplierpurchase.repository.PurchaseOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GrnServiceImpl implements GrnService {

    private static final Logger log = LoggerFactory.getLogger(GrnServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final GoodsReceivedNoteRepository grnRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final RestTemplate restTemplate;

    @Value("${supplier.grn.inventory-sync.enabled:true}")
    private boolean inventorySyncEnabled;

    @Value("${supplier.grn.inventory-sync.base-url:http://localhost:8083}")
    private String inventoryServiceBaseUrl;

    @Override
    @Transactional
    public GrnResponse createGrn(GrnRequest request) {
        PurchaseOrder order = getOrder(request.getPurchaseOrderId());
        if (!order.getStoreId().equals(request.getStoreId())) {
            throw new ResourceNotFoundException("Purchase order does not belong to storeId=" + request.getStoreId());
        }

        GoodsReceivedNote grn = new GoodsReceivedNote();
        grn.setStoreId(request.getStoreId());
        grn.setPurchaseOrderId(request.getPurchaseOrderId());
        grn.setGrnNumber(generateGrnNumber(request.getReceivedDate()));
        grn.setReceivedDate(request.getReceivedDate());
        grn.setSupplierInvoiceNumber(request.getSupplierInvoiceNumber());
        grn.setSupplierInvoiceDate(request.getSupplierInvoiceDate());
        grn.setStatus(GrnStatus.DRAFT);
        grn.setReceivedBy(request.getReceivedBy());
        grn.setNotes(request.getNotes());

        List<GrnItem> items = request.getItems().stream().map(item -> mapItem(grn, item)).toList();
        grn.getItems().clear();
        grn.getItems().addAll(items);
        grn.setTotalAmount(scale(items.stream().map(GrnItem::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add)));

        return toResponse(grnRepository.save(grn));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrnResponse> listGrns(Long storeId) {
        return grnRepository.findByStoreIdOrderByReceivedDateDescIdDesc(storeId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GrnResponse getGrn(Long id) {
        return toResponse(getGrnEntity(id));
    }

    @Override
    @Transactional
    public GrnResponse verifyGrn(Long id) {
        GoodsReceivedNote grn = getGrnEntity(id);
        if (grn.getStatus() == GrnStatus.APPROVED) {
            return toResponse(grn);
        }
        if (grn.getStatus() != GrnStatus.VERIFIED) {
            grn.setStatus(GrnStatus.VERIFIED);
            grn = grnRepository.save(grn);
        }
        return toResponse(grn);
    }

    @Override
    @Transactional
    public GrnResponse approveGrn(Long id) {
        GoodsReceivedNote grn = getGrnEntity(id);
        if (grn.getStatus() == GrnStatus.APPROVED) {
            return toResponse(grn);
        }
        if (grn.getStatus() != GrnStatus.VERIFIED) {
            throw new IllegalArgumentException("GRN must be VERIFIED before approval");
        }

        PurchaseOrder order = getOrder(grn.getPurchaseOrderId());
        for (GrnItem grnItem : grn.getItems()) {
            PurchaseOrderItem poItem = resolveOrderItem(order, grnItem);
            double previous = poItem.getQuantityReceived() == null ? 0D : poItem.getQuantityReceived();
            poItem.setQuantityReceived(Math.min(poItem.getQuantityOrdered(), previous + grnItem.getQuantityAccepted()));
            syncInventoryForApprovedItem(grn, grnItem);
        }

        boolean fullyReceived = order.getItems().stream()
            .allMatch(item -> (item.getQuantityReceived() == null ? 0D : item.getQuantityReceived()) >= item.getQuantityOrdered());
        order.setStatus(fullyReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);

        grn.setStatus(GrnStatus.APPROVED);
        purchaseOrderRepository.save(order);
        return toResponse(grnRepository.save(grn));
    }

    private void syncInventoryForApprovedItem(GoodsReceivedNote grn, GrnItem grnItem) {
        if (!inventorySyncEnabled) {
            return;
        }
        if (grnItem.getQuantityAccepted() == null || grnItem.getQuantityAccepted() <= 0D) {
            return;
        }
        if (grnItem.getProductId() == null) {
            throw new IllegalArgumentException("productId is required for inventory sync: " + grnItem.getProductName());
        }

        InventoryAdjustHookRequest hookRequest = InventoryAdjustHookRequest.builder()
            .productId(grnItem.getProductId())
            .storeId(grn.getStoreId())
            .transactionType("PURCHASE")
            .quantity(grnItem.getQuantityAccepted())
            .referenceId(grn.getGrnNumber())
            .notes("GRN approved: " + grn.getGrnNumber())
            .createdBy(grn.getReceivedBy() == null ? "supplier-purchase-service" : grn.getReceivedBy())
            .build();

        try {
            restTemplate.postForEntity(inventoryServiceBaseUrl + "/api/inventory/adjust", hookRequest, Object.class);
        } catch (Exception ex) {
            // Keep approval flow resilient; retry mechanisms can be added later.
            log.warn("Inventory sync failed for GRN {} and product {}: {}", grn.getGrnNumber(), grnItem.getProductId(), ex.getMessage());
        }
    }

    private PurchaseOrderItem resolveOrderItem(PurchaseOrder order, GrnItem grnItem) {
        return order.getItems().stream()
            .filter(item -> {
                if (grnItem.getProductId() != null && item.getProductId() != null) {
                    return grnItem.getProductId().equals(item.getProductId());
                }
                return item.getProductName().equalsIgnoreCase(grnItem.getProductName());
            })
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("PO item not found for GRN product " + grnItem.getProductName()));
    }

    private PurchaseOrder getOrder(Long id) {
        return purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    private GoodsReceivedNote getGrnEntity(Long id) {
        return grnRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("GRN not found with id: " + id));
    }

    private GrnItem mapItem(GoodsReceivedNote grn, GrnItemRequest request) {
        double rejected = request.getQuantityRejected() == null ? 0D : request.getQuantityRejected();
        if (request.getQuantityAccepted() > request.getQuantityReceived()) {
            throw new IllegalArgumentException("quantityAccepted cannot exceed quantityReceived for product " + request.getProductName());
        }

        BigDecimal totalAmount = request.getUnitPrice()
            .multiply(BigDecimal.valueOf(request.getQuantityAccepted()))
            .setScale(2, RoundingMode.HALF_UP);

        return GrnItem.builder()
            .grn(grn)
            .productId(request.getProductId())
            .productName(request.getProductName())
            .quantityReceived(request.getQuantityReceived())
            .quantityAccepted(request.getQuantityAccepted())
            .quantityRejected(rejected)
            .rejectionReason(request.getRejectionReason())
            .batchNumber(request.getBatchNumber())
            .manufacturingDate(request.getManufacturingDate())
            .expiryDate(request.getExpiryDate())
            .unitPrice(scale(request.getUnitPrice()))
            .totalAmount(totalAmount)
            .build();
    }

    private GrnResponse toResponse(GoodsReceivedNote grn) {
        return GrnResponse.builder()
            .id(grn.getId())
            .storeId(grn.getStoreId())
            .purchaseOrderId(grn.getPurchaseOrderId())
            .grnNumber(grn.getGrnNumber())
            .receivedDate(grn.getReceivedDate())
            .supplierInvoiceNumber(grn.getSupplierInvoiceNumber())
            .supplierInvoiceDate(grn.getSupplierInvoiceDate())
            .totalAmount(grn.getTotalAmount())
            .status(grn.getStatus())
            .receivedBy(grn.getReceivedBy())
            .notes(grn.getNotes())
            .createdAt(grn.getCreatedAt())
            .updatedAt(grn.getUpdatedAt())
            .items(grn.getItems().stream().map(this::toItemResponse).toList())
            .build();
    }

    private GrnItemResponse toItemResponse(GrnItem item) {
        return GrnItemResponse.builder()
            .id(item.getId())
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantityReceived(item.getQuantityReceived())
            .quantityAccepted(item.getQuantityAccepted())
            .quantityRejected(item.getQuantityRejected())
            .rejectionReason(item.getRejectionReason())
            .batchNumber(item.getBatchNumber())
            .manufacturingDate(item.getManufacturingDate())
            .expiryDate(item.getExpiryDate())
            .unitPrice(item.getUnitPrice())
            .totalAmount(item.getTotalAmount())
            .build();
    }

    private String generateGrnNumber(LocalDate receivedDate) {
        long count = grnRepository.countByReceivedDate(receivedDate) + 1;
        return String.format("GRN-%s-%03d", DATE_FORMATTER.format(receivedDate), count);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

