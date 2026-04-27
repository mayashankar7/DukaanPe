package com.dukaanpe.udhar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.udhar.dto.CreateKhataCustomerRequest;
import com.dukaanpe.udhar.dto.CreateReminderRequest;
import com.dukaanpe.udhar.dto.CreateSettlementRequest;
import com.dukaanpe.udhar.dto.CreditRequest;
import com.dukaanpe.udhar.dto.PaymentRequest;
import com.dukaanpe.udhar.entity.ReminderType;
import com.dukaanpe.udhar.entity.SettlementMode;
import com.dukaanpe.udhar.entity.UdharPaymentMode;
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
class UdharKhataControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateCustomerAndRunCreditPaymentReminderSettlementFlow() throws Exception {
        Long storeId = 77L;
        String month = LocalDate.now().toString().substring(0, 7);

        Long customerId = createCustomer(storeId, "Flow Customer", "9876500099", new BigDecimal("1000"));

        Long creditEntryId = createCredit(storeId, customerId, null, new BigDecimal("1200"), true);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setStoreId(storeId);
        paymentRequest.setKhataCustomerId(customerId);
        paymentRequest.setAmount(new BigDecimal("200"));
        paymentRequest.setPaymentMode(UdharPaymentMode.UPI);
        paymentRequest.setReferenceNumber("UPI-TEST-1");

        mockMvc.perform(post("/api/udhar/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.entryType").value("PARTIAL_PAYMENT"))
            .andExpect(jsonPath("$.data.runningBalance").value(1000));

        mockMvc.perform(get("/api/udhar/entries")
                .param("customerId", customerId.toString())
                .param("fromDate", LocalDate.now().minusDays(7).toString())
                .param("toDate", LocalDate.now().plusDays(1).toString())
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2));

        mockMvc.perform(get("/api/udhar/summary").param("storeId", storeId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalOutstanding").value(1000));

        mockMvc.perform(get("/api/udhar/overdue")
                .param("storeId", storeId.toString())
                .param("fromDate", LocalDate.now().minusDays(30).toString())
                .param("toDate", LocalDate.now().toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));

        CreateReminderRequest reminderRequest = new CreateReminderRequest();
        reminderRequest.setUdharEntryId(creditEntryId);
        reminderRequest.setKhataCustomerId(customerId);
        reminderRequest.setStoreId(storeId);
        reminderRequest.setReminderDate(LocalDate.now());
        reminderRequest.setReminderType(ReminderType.WHATSAPP);
        reminderRequest.setMessage("Please clear your pending khata amount");

        String reminderResponse = mockMvc.perform(post("/api/udhar/reminders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reminderRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.isSent").value(false))
            .andReturn().getResponse().getContentAsString();

        Long reminderId = objectMapper.readTree(reminderResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/udhar/reminders/pending")
                .param("storeId", storeId.toString())
                .param("fromDate", LocalDate.now().minusDays(1).toString())
                .param("toDate", LocalDate.now().plusDays(1).toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(put("/api/udhar/reminders/{id}/send", reminderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isSent").value(true));

        mockMvc.perform(get("/api/udhar/reminders/history")
                .param("customerId", customerId.toString())
                .param("fromDate", LocalDate.now().minusDays(10).toString())
                .param("toDate", LocalDate.now().plusDays(2).toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));

        CreateSettlementRequest settlementRequest = new CreateSettlementRequest();
        settlementRequest.setKhataCustomerId(customerId);
        settlementRequest.setStoreId(storeId);
        settlementRequest.setSettlementDate(LocalDate.now());
        settlementRequest.setAmountSettled(new BigDecimal("700"));
        settlementRequest.setDiscountGiven(new BigDecimal("50"));
        settlementRequest.setSettlementMode(SettlementMode.PARTIAL);
        settlementRequest.setNotes("Negotiated partial settlement");

        mockMvc.perform(post("/api/udhar/settlements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settlementRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.amountSettled").value(700));

        mockMvc.perform(get("/api/udhar/settlements")
                .param("customerId", customerId.toString())
                .param("fromDate", LocalDate.now().minusDays(2).toString())
                .param("toDate", LocalDate.now().plusDays(2).toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/udhar/settlements/report")
                .param("storeId", storeId.toString())
                .param("month", month)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.month").value(month))
            .andExpect(jsonPath("$.data.totalRecords").value(1));
    }

    @Test
    void shouldAcceptCreditByPhoneForBillingHookPath() throws Exception {
        Long storeId = 78L;
        createCustomer(storeId, "Phone Hook Customer", "9876500088", new BigDecimal("4000"));

        CreditRequest request = new CreditRequest();
        request.setStoreId(storeId);
        request.setCustomerPhone("9876500088");
        request.setAmount(new BigDecimal("450"));
        request.setDescription("Credit posted from billing by phone");
        request.setBillId(9999L);

        mockMvc.perform(post("/api/udhar/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.amount").value(450));
    }

    @Test
    void shouldValidateSearchAndPaginationAndDateEdges() throws Exception {
        mockMvc.perform(get("/api/khata/customers")
                .param("storeId", "1")
                .param("page", "-1")
                .param("size", "20"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/khata/customers/search")
                .param("storeId", "1")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/udhar/entries")
                .param("customerId", "1")
                .param("fromDate", "2026-03-20")
                .param("toDate", "2026-03-01")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/udhar/settlements/report")
                .param("storeId", "1")
                .param("month", "Mar-2026")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    private Long createCustomer(Long storeId, String name, String phone, BigDecimal creditLimit) throws Exception {
        CreateKhataCustomerRequest createRequest = new CreateKhataCustomerRequest();
        createRequest.setStoreId(storeId);
        createRequest.setCustomerName(name);
        createRequest.setCustomerPhone(phone);
        createRequest.setCreditLimit(creditLimit);

        String createResponse = mockMvc.perform(post("/api/khata/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(createResponse).path("data").path("id").asLong();
    }

    private Long createCredit(Long storeId, Long customerId, String customerPhone, BigDecimal amount, boolean expectLimitBreach) throws Exception {
        CreditRequest creditRequest = new CreditRequest();
        creditRequest.setStoreId(storeId);
        creditRequest.setKhataCustomerId(customerId);
        creditRequest.setCustomerPhone(customerPhone);
        creditRequest.setAmount(amount);
        creditRequest.setDescription("Credit over limit");
        creditRequest.setDueDate(LocalDate.now().minusDays(1));

        String response = mockMvc.perform(post("/api/udhar/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.creditLimitBreached").value(expectLimitBreach))
            .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }
}
