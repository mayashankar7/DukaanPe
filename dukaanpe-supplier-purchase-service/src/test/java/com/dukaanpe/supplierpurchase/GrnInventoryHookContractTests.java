package com.dukaanpe.supplierpurchase;

import com.dukaanpe.supplierpurchase.dto.InventoryAdjustHookRequest;
import com.dukaanpe.supplierpurchase.entity.GoodsReceivedNote;
import com.dukaanpe.supplierpurchase.entity.GrnItem;
import com.dukaanpe.supplierpurchase.entity.GrnStatus;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrder;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrderItem;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrderStatus;
import com.dukaanpe.supplierpurchase.repository.GoodsReceivedNoteRepository;
import com.dukaanpe.supplierpurchase.repository.PurchaseOrderRepository;
import com.dukaanpe.supplierpurchase.service.GrnServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrnInventoryHookContractTests {

    @Mock
    private GoodsReceivedNoteRepository grnRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GrnServiceImpl grnService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(grnService, "inventorySyncEnabled", true);
        ReflectionTestUtils.setField(grnService, "inventoryServiceBaseUrl", "http://localhost:8083");
    }

    @Test
    void shouldRequireVerifiedStatusBeforeApprove() {
        GoodsReceivedNote draftGrn = sampleGrn(GrnStatus.DRAFT);

        when(grnRepository.findById(10L)).thenReturn(Optional.of(draftGrn));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> grnService.approveGrn(10L));
        assertEquals("GRN must be VERIFIED before approval", ex.getMessage());
        verify(restTemplate, never()).postForEntity(any(String.class), any(), eq(Object.class));
    }

    @Test
    void shouldPostInventoryAdjustPayloadOnApprove() {
        GoodsReceivedNote verifiedGrn = sampleGrn(GrnStatus.VERIFIED);
        PurchaseOrder po = samplePurchaseOrder();

        when(grnRepository.findById(10L)).thenReturn(Optional.of(verifiedGrn));
        when(purchaseOrderRepository.findById(20L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(grnRepository.save(any(GoodsReceivedNote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        grnService.approveGrn(10L);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InventoryAdjustHookRequest> payloadCaptor = ArgumentCaptor.forClass(InventoryAdjustHookRequest.class);

        verify(restTemplate, times(1)).postForEntity(urlCaptor.capture(), payloadCaptor.capture(), eq(Object.class));
        assertEquals("http://localhost:8083/api/inventory/adjust", urlCaptor.getValue());
        assertEquals(9001L, payloadCaptor.getValue().getProductId());
        assertEquals(1L, payloadCaptor.getValue().getStoreId());
        assertEquals("PURCHASE", payloadCaptor.getValue().getTransactionType());
        assertEquals(7.0, payloadCaptor.getValue().getQuantity());
        assertEquals("GRN-20260326-001", payloadCaptor.getValue().getReferenceId());

        assertEquals(PurchaseOrderStatus.PARTIALLY_RECEIVED, po.getStatus());
        assertEquals(7.0, po.getItems().get(0).getQuantityReceived());
    }

    private GoodsReceivedNote sampleGrn(GrnStatus status) {
        GoodsReceivedNote grn = GoodsReceivedNote.builder()
            .id(10L)
            .storeId(1L)
            .purchaseOrderId(20L)
            .grnNumber("GRN-20260326-001")
            .receivedDate(LocalDate.of(2026, 3, 26))
            .status(status)
            .receivedBy("contract-tester")
            .totalAmount(new BigDecimal("700.00"))
            .build();

        GrnItem item = GrnItem.builder()
            .id(30L)
            .grn(grn)
            .productId(9001L)
            .productName("Test Refined Oil")
            .quantityReceived(10.0)
            .quantityAccepted(7.0)
            .quantityRejected(3.0)
            .unitPrice(new BigDecimal("100.00"))
            .totalAmount(new BigDecimal("700.00"))
            .build();
        grn.setItems(List.of(item));
        return grn;
    }

    private PurchaseOrder samplePurchaseOrder() {
        PurchaseOrder order = PurchaseOrder.builder()
            .id(20L)
            .storeId(1L)
            .supplierId(1L)
            .poNumber("PO-20260326-001")
            .orderDate(LocalDate.of(2026, 3, 26))
            .status(PurchaseOrderStatus.SENT)
            .subtotal(new BigDecimal("1000.00"))
            .gstAmount(new BigDecimal("50.00"))
            .totalAmount(new BigDecimal("1050.00"))
            .build();

        PurchaseOrderItem item = PurchaseOrderItem.builder()
            .id(40L)
            .purchaseOrder(order)
            .productId(9001L)
            .productName("Test Refined Oil")
            .quantityOrdered(10.0)
            .quantityReceived(0.0)
            .unit("LITRE")
            .unitPrice(new BigDecimal("100.00"))
            .gstRate(new BigDecimal("5.00"))
            .totalAmount(new BigDecimal("1050.00"))
            .build();

        order.setItems(List.of(item));
        return order;
    }
}

