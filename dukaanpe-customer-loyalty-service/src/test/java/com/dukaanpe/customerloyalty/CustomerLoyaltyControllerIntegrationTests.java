package com.dukaanpe.customerloyalty;

import com.dukaanpe.customerloyalty.dto.CreateCampaignRequest;
import com.dukaanpe.customerloyalty.dto.CreateCustomerRequest;
import com.dukaanpe.customerloyalty.dto.EarnLoyaltyPointsRequest;
import com.dukaanpe.customerloyalty.dto.LoyaltySettingsRequest;
import com.dukaanpe.customerloyalty.dto.RecordPurchaseRequest;
import com.dukaanpe.customerloyalty.dto.RedeemLoyaltyPointsRequest;
import com.dukaanpe.customerloyalty.dto.UpdateCampaignRequest;
import com.dukaanpe.customerloyalty.dto.UpdateCustomerRequest;
import com.dukaanpe.customerloyalty.entity.CampaignType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerLoyaltyControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRunCustomerCrudFlow() throws Exception {
        Long customerId = createCustomer(91L, "9876510001", "Test Customer");

        mockMvc.perform(get("/api/customers/{id}", customerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.customerName").value("Test Customer"));

        UpdateCustomerRequest update = new UpdateCustomerRequest();
        update.setCustomerName("Updated Customer");
        update.setPhone("9876510001");
        update.setAddress("Updated Address");

        mockMvc.perform(put("/api/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.customerName").value("Updated Customer"));

        mockMvc.perform(get("/api/customers")
                .param("storeId", "91")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(delete("/api/customers/{id}", customerId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", customerId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldRecordPurchaseAndReturnPagedHistory() throws Exception {
        Long customerId = createCustomer(92L, "9876510002", "Purchase Customer");

        RecordPurchaseRequest request = new RecordPurchaseRequest();
        request.setBillId(1001L);
        request.setBillNumber("BILL-1001");
        request.setBillDate(LocalDate.now());
        request.setTotalAmount(new BigDecimal("1200.00"));
        request.setItemsSummary("Oil, Rice, Sugar");
        request.setPaymentMode("UPI");

        mockMvc.perform(post("/api/customers/{id}/purchases", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.billNumber").value("BILL-1001"));

        mockMvc.perform(get("/api/customers/{id}/purchases", customerId)
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].totalAmount").value(1200.00));

        mockMvc.perform(get("/api/customers/{id}", customerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalVisits").value(1))
            .andExpect(jsonPath("$.data.totalPurchases").value(1200.00));
    }

    @Test
    void shouldEarnRedeemAndListLoyaltyTransactions() throws Exception {
        Long customerId = createCustomer(93L, "9876510003", "Loyalty Customer");

        EarnLoyaltyPointsRequest earn = new EarnLoyaltyPointsRequest();
        earn.setCustomerId(customerId);
        earn.setPurchaseAmount(new BigDecimal("1250.00"));
        earn.setReferenceBillId(2001L);
        earn.setDescription("Earn from bill 2001");

        mockMvc.perform(post("/api/loyalty/earn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(earn)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.points").value(12))
            .andExpect(jsonPath("$.data.runningBalance").value(12));

        RedeemLoyaltyPointsRequest redeem = new RedeemLoyaltyPointsRequest();
        redeem.setCustomerId(customerId);
        redeem.setPoints(5);
        redeem.setReferenceBillId(2002L);
        redeem.setDescription("Redeem on bill 2002");

        mockMvc.perform(post("/api/loyalty/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(redeem)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.runningBalance").value(7));

        mockMvc.perform(get("/api/loyalty/customer/{customerId}", customerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.loyaltyPoints").value(7));

        mockMvc.perform(get("/api/loyalty/transactions")
                .param("customerId", customerId.toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void shouldRejectRedeemWhenPointsInsufficient() throws Exception {
        Long customerId = createCustomer(94L, "9876510004", "Insufficient Points");

        RedeemLoyaltyPointsRequest redeem = new RedeemLoyaltyPointsRequest();
        redeem.setCustomerId(customerId);
        redeem.setPoints(20);

        mockMvc.perform(post("/api/loyalty/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(redeem)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldValidatePaginationAndPhone() throws Exception {
        mockMvc.perform(get("/api/customers")
                .param("storeId", "1")
                .param("page", "-1")
                .param("size", "10"))
            .andExpect(status().isBadRequest());

        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setStoreId(95L);
        request.setCustomerName("Bad Phone");
        request.setPhone("123");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldSearchByQueryAndHandlePaginationEdges() throws Exception {
        createCustomer(96L, "9876511001", "Ravi Kumar");
        createCustomer(96L, "9876511002", "Ravi Shankar");
        createCustomer(96L, "9876511003", "Meena Das");

        mockMvc.perform(get("/api/customers/search")
                .param("storeId", "96")
                .param("q", "Ravi")
                .param("page", "0")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.content.length()").value(1));

        mockMvc.perform(get("/api/customers/search")
                .param("storeId", "96")
                .param("q", "")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/customers/search")
                .param("storeId", "96")
                .param("q", "Ravi")
                .param("page", "0")
                .param("size", "101"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindByPhoneAndReturnNotFoundWhenMissing() throws Exception {
        createCustomer(97L, "9876512001", "Phone Match");

        mockMvc.perform(get("/api/customers/phone/{phone}", "9876512001")
                .param("storeId", "97"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.customerName").value("Phone Match"));

        mockMvc.perform(get("/api/customers/phone/{phone}", "9876512009")
                .param("storeId", "97"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnTopCustomersByPurchaseAndValidateLimit() throws Exception {
        Long c1 = createCustomer(98L, "9876513001", "Top One");
        Long c2 = createCustomer(98L, "9876513002", "Top Two");
        Long c3 = createCustomer(98L, "9876513003", "Top Three");

        recordPurchase(c1, "1000.00", 3001L);
        recordPurchase(c2, "2000.00", 3002L);
        recordPurchase(c3, "1500.00", 3003L);

        mockMvc.perform(get("/api/customers/top")
                .param("storeId", "98")
                .param("limit", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].customerName").value("Top Two"))
            .andExpect(jsonPath("$.data[1].customerName").value("Top Three"));

        mockMvc.perform(get("/api/customers/top")
                .param("storeId", "98")
                .param("limit", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateSettingsAndApplyTierAndPointsRules() throws Exception {
        Long customerId = createCustomer(99L, "9876514001", "Settings Customer");

        LoyaltySettingsRequest settings = new LoyaltySettingsRequest();
        settings.setStoreId(99L);
        settings.setPointsPerHundred(2);
        settings.setPointsToRedeemUnit(200);
        settings.setRedeemValueRupees(25);
        settings.setSilverThreshold(new BigDecimal("2000"));
        settings.setGoldThreshold(new BigDecimal("5000"));
        settings.setPlatinumThreshold(new BigDecimal("10000"));

        mockMvc.perform(put("/api/loyalty/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pointsPerHundred").value(2));

        recordPurchase(customerId, "2500.00", 9901L);
        mockMvc.perform(get("/api/customers/{id}", customerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.customerTier").value("SILVER"));

        EarnLoyaltyPointsRequest earn = new EarnLoyaltyPointsRequest();
        earn.setCustomerId(customerId);
        earn.setPurchaseAmount(new BigDecimal("1200.00"));
        earn.setReferenceBillId(9902L);

        mockMvc.perform(post("/api/loyalty/earn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(earn)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.points").value(24));
    }

    @Test
    void shouldManageCampaignCrudAndActiveFilters() throws Exception {
        Long campaignId = createCampaign(100L, "Festive Boost", LocalDate.now().minusDays(1), LocalDate.now().plusDays(3));

        mockMvc.perform(get("/api/loyalty/campaigns")
                .param("storeId", "100")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/loyalty/campaigns/active")
                .param("storeId", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));

        UpdateCampaignRequest update = new UpdateCampaignRequest();
        update.setCampaignName("Festive Boost Updated");
        update.setCampaignType(CampaignType.FESTIVAL);
        update.setDescription("Updated message");
        update.setDiscountPercent(new BigDecimal("6.00"));
        update.setStartDate(LocalDate.now().minusDays(1));
        update.setEndDate(LocalDate.now().plusDays(5));
        update.setTargetTier("ALL");
        update.setMessageTemplate("New offer");

        mockMvc.perform(put("/api/loyalty/campaigns/{id}", campaignId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.campaignName").value("Festive Boost Updated"));

        mockMvc.perform(delete("/api/loyalty/campaigns/{id}", campaignId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/loyalty/campaigns/active")
                .param("storeId", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void shouldRejectInvalidCampaignDateRangeAndThresholdOrder() throws Exception {
        CreateCampaignRequest campaign = new CreateCampaignRequest();
        campaign.setStoreId(101L);
        campaign.setCampaignName("Bad Dates");
        campaign.setCampaignType(CampaignType.CUSTOM);
        campaign.setStartDate(LocalDate.now().plusDays(2));
        campaign.setEndDate(LocalDate.now());
        campaign.setTargetTier("ALL");

        mockMvc.perform(post("/api/loyalty/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(campaign)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        LoyaltySettingsRequest settings = new LoyaltySettingsRequest();
        settings.setStoreId(101L);
        settings.setPointsPerHundred(1);
        settings.setPointsToRedeemUnit(100);
        settings.setRedeemValueRupees(10);
        settings.setSilverThreshold(new BigDecimal("10000"));
        settings.setGoldThreshold(new BigDecimal("5000"));
        settings.setPlatinumThreshold(new BigDecimal("15000"));

        mockMvc.perform(put("/api/loyalty/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldPreviewEligibleCustomersForCampaignByTierAndMinPurchase() throws Exception {
        Long c1 = createCustomer(104L, "9876516001", "Preview One");
        Long c2 = createCustomer(104L, "9876516002", "Preview Two");
        Long c3 = createCustomer(104L, "9876516003", "Preview Three");

        updateSettings(104L, 1, "1000", "5000", "10000");
        recordPurchase(c1, "1500.00", 5101L);
        recordPurchase(c2, "700.00", 5102L);
        recordPurchase(c3, "2200.00", 5103L);

        Long campaignId = createCampaign(
            104L,
            "Tier Filter Preview",
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(10),
            CampaignType.FESTIVAL,
            "SILVER",
            new BigDecimal("1200.00")
        );

        mockMvc.perform(get("/api/loyalty/campaigns/{id}/eligible-customers/preview", campaignId)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.content[0].customerName").value("Preview Three"))
            .andExpect(jsonPath("$.data.content[1].customerName").value("Preview One"));

        mockMvc.perform(get("/api/loyalty/campaigns/{id}/eligible-customers/preview/count", campaignId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.count").value(2));
    }

    @Test
    void shouldPreviewOnlyTodayBirthdaysForBirthdayCampaign() throws Exception {
        LocalDate today = LocalDate.now();
        createCustomer(105L, "9876517001", "Birthday Match", LocalDate.of(1994, today.getMonthValue(), today.getDayOfMonth()));
        createCustomer(105L, "9876517002", "Birthday Miss", LocalDate.of(1994, today.plusDays(1).getMonthValue(), today.plusDays(1).getDayOfMonth()));

        Long campaignId = createCampaign(
            105L,
            "Birthday Blast",
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(10),
            CampaignType.BIRTHDAY,
            "ALL",
            null
        );

        mockMvc.perform(get("/api/loyalty/campaigns/{id}/eligible-customers/preview", campaignId)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].customerName").value("Birthday Match"));

        mockMvc.perform(get("/api/loyalty/campaigns/{id}/eligible-customers/preview/count", campaignId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.count").value(1));
    }

    @Test
    void shouldRejectPreviewForInvalidCampaignTargetTier() throws Exception {
        Long campaignId = createCampaign(
            106L,
            "Invalid Tier Campaign",
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(10),
            CampaignType.CUSTOM,
            "SILVER,VIP",
            null
        );

        mockMvc.perform(get("/api/loyalty/campaigns/{id}/eligible-customers/preview", campaignId)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldApplyAdvancedSearchFiltersWithPaging() throws Exception {
        Long s1 = createCustomer(102L, "9876515001", "Filter Silver");
        Long s2 = createCustomer(102L, "9876515002", "Filter Regular");
        Long s3 = createCustomer(102L, "9876515003", "Filter Old");

        updateSettings(102L, 1, "1000", "5000", "10000");

        recordPurchase(s1, "1500.00", 4101L);
        recordPurchase(s2, "700.00", 4102L);
        // s3 intentionally has no visit date and no purchases

        mockMvc.perform(get("/api/customers/search")
                .param("storeId", "102")
                .param("tier", "SILVER")
                .param("minPurchases", "1000")
                .param("lastVisitFrom", LocalDate.now().minusDays(1).toString())
                .param("lastVisitTo", LocalDate.now().toString())
                .param("page", "0")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].customerName").value("Filter Silver"));

        mockMvc.perform(get("/api/customers/search")
                .param("storeId", "102")
                .param("tier", "SILVER")
                .param("page", "1")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void shouldRejectInvalidAdvancedSearchFilters() throws Exception {
        mockMvc.perform(get("/api/customers/search")
                .param("storeId", "103")
                .param("tier", "INVALID_TIER")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/customers/search")
                .param("storeId", "103")
                .param("lastVisitFrom", "2026-03-20")
                .param("lastVisitTo", "2026-03-01")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/customers/search")
                .param("storeId", "103")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    private Long createCustomer(Long storeId, String phone, String name) throws Exception {
        return createCustomer(storeId, phone, name, null);
    }

    private Long createCustomer(Long storeId, String phone, String name, LocalDate dateOfBirth) throws Exception {
        CreateCustomerRequest create = new CreateCustomerRequest();
        create.setStoreId(storeId);
        create.setCustomerName(name);
        create.setPhone(phone);
        create.setAddress("Pune");
        create.setDateOfBirth(dateOfBirth);

        String response = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private void recordPurchase(Long customerId, String amount, Long billId) throws Exception {
        RecordPurchaseRequest request = new RecordPurchaseRequest();
        request.setBillId(billId);
        request.setBillNumber("BILL-" + billId);
        request.setBillDate(LocalDate.now());
        request.setTotalAmount(new BigDecimal(amount));
        request.setItemsSummary("sample");
        request.setPaymentMode("CASH");

        mockMvc.perform(post("/api/customers/{id}/purchases", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    private Long createCampaign(Long storeId, String name, LocalDate start, LocalDate end) throws Exception {
        return createCampaign(storeId, name, start, end, CampaignType.FESTIVAL, "ALL", null);
    }

    private Long createCampaign(
        Long storeId,
        String name,
        LocalDate start,
        LocalDate end,
        CampaignType campaignType,
        String targetTier,
        BigDecimal minPurchase
    ) throws Exception {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setStoreId(storeId);
        request.setCampaignName(name);
        request.setCampaignType(campaignType);
        request.setDescription("Campaign desc");
        request.setDiscountPercent(new BigDecimal("5.00"));
        request.setMinPurchaseAmount(minPurchase);
        request.setStartDate(start);
        request.setEndDate(end);
        request.setTargetTier(targetTier);
        request.setMessageTemplate("Campaign msg");

        String response = mockMvc.perform(post("/api/loyalty/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("id").asLong();
    }

    private void updateSettings(Long storeId, int pointsPerHundred, String silver, String gold, String platinum) throws Exception {
        LoyaltySettingsRequest settings = new LoyaltySettingsRequest();
        settings.setStoreId(storeId);
        settings.setPointsPerHundred(pointsPerHundred);
        settings.setPointsToRedeemUnit(100);
        settings.setRedeemValueRupees(10);
        settings.setSilverThreshold(new BigDecimal(silver));
        settings.setGoldThreshold(new BigDecimal(gold));
        settings.setPlatinumThreshold(new BigDecimal(platinum));

        mockMvc.perform(put("/api/loyalty/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings)))
            .andExpect(status().isOk());
    }
}
