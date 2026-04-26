package com.dukaanpe.billing;

import com.dukaanpe.billing.dto.BillItemRequest;
import com.dukaanpe.billing.dto.CreateBillRequest;
import com.dukaanpe.billing.entity.Bill;
import com.dukaanpe.billing.entity.PaymentMode;
import com.dukaanpe.billing.repository.BillRepository;
import com.dukaanpe.billing.service.BillingServiceImpl;
import java.math.BigDecimal;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingHooksContractTests {

    @Mock
    private BillRepository billRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BillingServiceImpl billingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(billingService, "udharHookEnabled", false);
        ReflectionTestUtils.setField(billingService, "supplierHookEnabled", true);
        ReflectionTestUtils.setField(billingService, "supplierHookBaseUrl", "http://localhost:8086");
        ReflectionTestUtils.setField(billingService, "supplierHookLimit", 4);

        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            bill.setId(777L);
            return bill;
        });
    }

    @Test
    void shouldCallSupplierAutoSuggestWithExpectedUrlTemplateAndParams() {
        billingService.createBill(createCashBillRequest(15L));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate, times(1)).getForEntity(urlCaptor.capture(), eq(Object.class), eq(15L), eq(4));
        assertEquals("http://localhost:8086/api/purchase-orders/auto-suggest?storeId={storeId}&limit={limit}", urlCaptor.getValue());
    }

    @Test
    void shouldNotCallSupplierAutoSuggestWhenHookDisabled() {
        ReflectionTestUtils.setField(billingService, "supplierHookEnabled", false);

        billingService.createBill(createCashBillRequest(15L));

        verify(restTemplate, never()).getForEntity(any(String.class), eq(Object.class), any(), any());
    }

    private CreateBillRequest createCashBillRequest(Long storeId) {
        CreateBillRequest request = new CreateBillRequest();
        request.setStoreId(storeId);
        request.setPaymentMode(PaymentMode.CASH);
        request.setCustomerName("Contract Test");
        request.setCustomerPhone("9876543219");
        request.setCashAmount(new BigDecimal("100"));
        request.setItems(List.of(item()));
        return request;
    }

    private BillItemRequest item() {
        BillItemRequest item = new BillItemRequest();
        item.setProductId(5001L);
        item.setProductName("Contract Item");
        item.setQuantity(1.0);
        item.setUnit("PIECE");
        item.setMrp(new BigDecimal("95"));
        item.setUnitPrice(new BigDecimal("95"));
        item.setDiscountPercent(BigDecimal.ZERO);
        item.setGstRate(new BigDecimal("5"));
        return item;
    }
}

