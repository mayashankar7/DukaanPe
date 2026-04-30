package com.dukaanpe.payment;

import com.dukaanpe.payment.dto.CompletePaymentRequest;
import com.dukaanpe.payment.dto.CreateReconciliationRequest;
import com.dukaanpe.payment.dto.FailPaymentRequest;
import com.dukaanpe.payment.dto.GenerateUpiLinkRequest;
import com.dukaanpe.payment.dto.InitiatePaymentRequest;
import com.dukaanpe.payment.dto.OpenCashRegisterRequest;
import com.dukaanpe.payment.dto.RefundPaymentRequest;
import com.dukaanpe.payment.dto.SaveUpiQrCodeRequest;
import com.dukaanpe.payment.dto.CloseCashRegisterRequest;
import com.dukaanpe.payment.entity.PaymentMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldInitiateAndFetchPayment() throws Exception {
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setStoreId(71L);
        request.setAmount(new BigDecimal("499.00"));
        request.setPaymentMode(PaymentMode.UPI);
        request.setPayerName("Integration Tester");
        request.setPayerPhone("9876543299");

        String response = mockMvc.perform(post("/api/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.paymentStatus").value("INITIATED"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String transactionId = objectMapper.readTree(response).path("data").path("transactionId").asText();

        mockMvc.perform(get("/api/payments/{transactionId}", transactionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.transactionId").value(transactionId));
    }

    @Test
    void shouldRespectIdempotencyForPaymentInitiate() throws Exception {
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setStoreId(711L);
        request.setAmount(new BigDecimal("209.00"));
        request.setPaymentMode(PaymentMode.CASH);
        request.setPayerName("Idempotent Customer");
        request.setPayerPhone("9876543298");

        String key = "idem-initiate-711";

        String firstResponse = mockMvc.perform(post("/api/payments/initiate")
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/payments/initiate")
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String firstTxn = objectMapper.readTree(firstResponse).path("data").path("transactionId").asText();
        String secondTxn = objectMapper.readTree(secondResponse).path("data").path("transactionId").asText();

        org.junit.jupiter.api.Assertions.assertEquals(firstTxn, secondTxn);
    }

    @Test
    void shouldCompleteAndRefundPayment() throws Exception {
        String transactionId = createPayment(72L, "650.00");

        CompletePaymentRequest completeRequest = new CompletePaymentRequest();
        completeRequest.setUpiReference("UPI-COMPLETE-8899");

        mockMvc.perform(put("/api/payments/{transactionId}/complete", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETED"));

        RefundPaymentRequest refundRequest = new RefundPaymentRequest();
        refundRequest.setRefundAmount(new BigDecimal("250.00"));
        refundRequest.setReason("Customer returned item");

        mockMvc.perform(post("/api/payments/{transactionId}/refund", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentStatus").value("REFUNDED"));
    }

    @Test
    void shouldFailInitiatedPaymentAndBlockInvalidTransitions() throws Exception {
        String transactionId = createPayment(73L, "300.00");

        FailPaymentRequest failRequest = new FailPaymentRequest();
        failRequest.setReason("UPI timeout");

        mockMvc.perform(put("/api/payments/{transactionId}/fail", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(failRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentStatus").value("FAILED"));

        CompletePaymentRequest completeRequest = new CompletePaymentRequest();
        completeRequest.setUpiReference("UPI-NOT-ALLOWED");

        mockMvc.perform(put("/api/payments/{transactionId}/complete", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldListPaymentsByDateAndPaginationEdges() throws Exception {
        Long storeId = 74L;
        createPayment(storeId, "120.00");
        createPayment(storeId, "220.00");
        createPayment(storeId, "320.00");

        String today = LocalDate.now().toString();

        mockMvc.perform(get("/api/payments")
                .param("storeId", storeId.toString())
                .param("date", today)
                .param("page", "0")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.pageNumber").value(0))
            .andExpect(jsonPath("$.data.pageSize").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.content.length()").value(2));

        mockMvc.perform(get("/api/payments")
                .param("storeId", storeId.toString())
                .param("date", today)
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.last").value(true));

        mockMvc.perform(get("/api/payments")
                .param("storeId", storeId.toString())
                .param("date", "2001-01-01")
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void shouldValidatePayloadAndPagination() throws Exception {
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setStoreId(1L);
        request.setAmount(new BigDecimal("10.00"));
        request.setPaymentMode(PaymentMode.CASH);
        request.setPayerPhone("12345");

        mockMvc.perform(post("/api/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/payments")
                .param("storeId", "1")
                .param("page", "-1")
                .param("size", "20"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/payments")
                .param("storeId", "1")
                .param("page", "0")
                .param("size", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldGenerateUpiLinkAndManageSavedQrCodes() throws Exception {
        GenerateUpiLinkRequest linkRequest = new GenerateUpiLinkRequest();
        linkRequest.setMerchantUpiId("rajesh@upi");
        linkRequest.setMerchantName("Rajesh General Store");
        linkRequest.setAmount(new BigDecimal("299.50"));
        linkRequest.setDescription("Bill Payment");

        mockMvc.perform(post("/api/payments/upi/generate-link")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(linkRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.upiLink").value(org.hamcrest.Matchers.containsString("upi://pay?pa=rajesh@upi")));

        mockMvc.perform(post("/api/payments/upi/generate-qr")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(linkRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.qrCodeData").value(org.hamcrest.Matchers.containsString("cu=INR")));

        SaveUpiQrCodeRequest saveRequest = new SaveUpiQrCodeRequest();
        saveRequest.setStoreId(81L);
        saveRequest.setMerchantUpiId("store81@upi");
        saveRequest.setMerchantName("Store 81");
        saveRequest.setQrCodeImageBase64("base64-qr-payload");
        saveRequest.setIsDefault(true);

        mockMvc.perform(post("/api/payments/upi/qr-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saveRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.storeId").value(81))
            .andExpect(jsonPath("$.data.isDefault").value(true));

        mockMvc.perform(get("/api/payments/upi/qr-codes")
                .param("storeId", "81"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].merchantUpiId").value("store81@upi"));
    }

    @Test
    void shouldCreateAndFetchMonthlyReconciliation() throws Exception {
        CreateReconciliationRequest request = new CreateReconciliationRequest();
        request.setStoreId(82L);
        request.setReconDate(LocalDate.now());
        request.setTotalCash(new BigDecimal("1000.00"));
        request.setTotalUpi(new BigDecimal("2000.00"));
        request.setTotalCard(new BigDecimal("500.00"));
        request.setCashInHand(new BigDecimal("950.00"));
        request.setNotes("End of day check");

        mockMvc.perform(post("/api/payments/reconciliation")
                .header("Idempotency-Key", "idem-recon-82")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalCollections").value(3500.00))
            .andExpect(jsonPath("$.data.status").value("DISCREPANCY"));

        mockMvc.perform(get("/api/payments/reconciliation")
                .param("storeId", "82")
                .param("month", LocalDate.now().toString().substring(0, 7)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void shouldBlockDuplicateReconciliationForStoreDate() throws Exception {
        CreateReconciliationRequest request = new CreateReconciliationRequest();
        request.setStoreId(821L);
        request.setReconDate(LocalDate.now());
        request.setTotalCash(new BigDecimal("100.00"));
        request.setTotalUpi(new BigDecimal("200.00"));
        request.setTotalCard(BigDecimal.ZERO);
        request.setCashInHand(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/payments/reconciliation")
                .header("Idempotency-Key", "idem-recon-821")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/payments/reconciliation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldOpenGetCurrentAndCloseCashRegister() throws Exception {
        Long storeId = 83L;
        createAndCompleteCashPayment(storeId, "250.00", "T1");
        createAndCompleteCashPayment(storeId, "150.00", "T1");
        createAndCompleteCashPayment(storeId, "120.00", "T2");

        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setStoreId(storeId);
        openRequest.setRegisterDate(LocalDate.now());
        openRequest.setOpeningBalance(new BigDecimal("500.00"));
        openRequest.setTerminalId("T1");

        mockMvc.perform(post("/api/payments/cash-register/open")
                .header("Idempotency-Key", "idem-open-83-t1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.terminalId").value("T1"))
            .andExpect(jsonPath("$.data.isClosed").value(false));

        OpenCashRegisterRequest terminalTwo = new OpenCashRegisterRequest();
        terminalTwo.setStoreId(storeId);
        terminalTwo.setRegisterDate(LocalDate.now());
        terminalTwo.setOpeningBalance(new BigDecimal("300.00"));
        terminalTwo.setTerminalId("T2");

        mockMvc.perform(post("/api/payments/cash-register/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(terminalTwo)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.terminalId").value("T2"));

        mockMvc.perform(get("/api/payments/cash-register/current")
                .param("storeId", storeId.toString())
                .param("terminalId", "T1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.storeId").value(83))
            .andExpect(jsonPath("$.data.terminalId").value("T1"))
            .andExpect(jsonPath("$.data.isClosed").value(false));

        CloseCashRegisterRequest closeRequest = new CloseCashRegisterRequest();
        closeRequest.setStoreId(storeId);
        closeRequest.setActualCashInDrawer(new BigDecimal("895.00"));
        closeRequest.setClosedBy("cashier-1");
        closeRequest.setTotalCashPaid(new BigDecimal("5.00"));
        closeRequest.setTerminalId("T1");

        mockMvc.perform(post("/api/payments/cash-register/close")
                .header("Idempotency-Key", "idem-close-83-t1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCashReceived").value(400.00))
            .andExpect(jsonPath("$.data.closingBalance").value(895.00))
            .andExpect(jsonPath("$.data.terminalId").value("T1"))
            .andExpect(jsonPath("$.data.isClosed").value(true));

        mockMvc.perform(get("/api/payments/cash-register/current")
                .param("storeId", storeId.toString())
                .param("terminalId", "T1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/payments/cash-register/current")
                .param("storeId", storeId.toString())
                .param("terminalId", "T2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.terminalId").value("T2"));
    }

    @Test
    void shouldValidateReconciliationMonthFormat() throws Exception {
        mockMvc.perform(get("/api/payments/reconciliation")
                .param("storeId", "1")
                .param("month", "2026/03"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldNormalizeBlankTerminalToDefaultAndKeepTerminalScopedTotals() throws Exception {
        Long storeId = 84L;
        createAndCompleteCashPayment(storeId, "90.00", null);
        createAndCompleteCashPayment(storeId, "60.00", "COUNTER-2");

        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setStoreId(storeId);
        openRequest.setRegisterDate(LocalDate.now());
        openRequest.setOpeningBalance(new BigDecimal("200.00"));
        openRequest.setTerminalId("   ");

        mockMvc.perform(post("/api/payments/cash-register/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.terminalId").value("DEFAULT"));

        mockMvc.perform(get("/api/payments/cash-register/current")
                .param("storeId", storeId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.terminalId").value("DEFAULT"));

        CloseCashRegisterRequest closeRequest = new CloseCashRegisterRequest();
        closeRequest.setStoreId(storeId);
        closeRequest.setActualCashInDrawer(new BigDecimal("290.00"));
        closeRequest.setClosedBy("cashier-default");
        closeRequest.setTotalCashPaid(BigDecimal.ZERO);
        closeRequest.setTerminalId("");

        mockMvc.perform(post("/api/payments/cash-register/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.terminalId").value("DEFAULT"))
            .andExpect(jsonPath("$.data.totalCashReceived").value(90.00))
            .andExpect(jsonPath("$.data.closingBalance").value(290.00));
    }

    @Test
    void shouldTrimTerminalIdSymmetricallyAcrossCashRegisterFlow() throws Exception {
        Long storeId = 85L;
        createAndCompleteCashPayment(storeId, "110.00", "T1");
        createAndCompleteCashPayment(storeId, "40.00", "T2");

        OpenCashRegisterRequest openRequest = new OpenCashRegisterRequest();
        openRequest.setStoreId(storeId);
        openRequest.setRegisterDate(LocalDate.now());
        openRequest.setOpeningBalance(new BigDecimal("300.00"));
        openRequest.setTerminalId("  T1  ");

        mockMvc.perform(post("/api/payments/cash-register/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.terminalId").value("T1"));

        mockMvc.perform(get("/api/payments/cash-register/current")
                .param("storeId", storeId.toString())
                .param("terminalId", " T1 "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.terminalId").value("T1"));

        CloseCashRegisterRequest closeRequest = new CloseCashRegisterRequest();
        closeRequest.setStoreId(storeId);
        closeRequest.setActualCashInDrawer(new BigDecimal("410.00"));
        closeRequest.setClosedBy("cashier-trim");
        closeRequest.setTotalCashPaid(BigDecimal.ZERO);
        closeRequest.setTerminalId("   T1");

        mockMvc.perform(post("/api/payments/cash-register/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.terminalId").value("T1"))
            .andExpect(jsonPath("$.data.totalCashReceived").value(110.00))
            .andExpect(jsonPath("$.data.closingBalance").value(410.00));
    }

    private String createPayment(Long storeId, String amount) throws Exception {
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setStoreId(storeId);
        request.setAmount(new BigDecimal(amount));
        request.setPaymentMode(PaymentMode.CASH);
        request.setPayerName("Test User");
        request.setPayerPhone("9876543288");

        String response = mockMvc.perform(post("/api/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).path("data").path("transactionId").asText();
    }

    private void createAndCompleteCashPayment(Long storeId, String amount, String terminalId) throws Exception {
        if (terminalId == null) {
            terminalId = "DEFAULT";
        }
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setStoreId(storeId);
        request.setAmount(new BigDecimal(amount));
        request.setPaymentMode(PaymentMode.CASH);
        request.setPayerName("Test User");
        request.setPayerPhone("9876543288");
        request.setTerminalId(terminalId);

        String response = mockMvc.perform(post("/api/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String transactionId = objectMapper.readTree(response).path("data").path("transactionId").asText();
        CompletePaymentRequest completeRequest = new CompletePaymentRequest();
        completeRequest.setUpiReference("N/A");
        mockMvc.perform(put("/api/payments/{transactionId}/complete", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETED"));
    }
}

