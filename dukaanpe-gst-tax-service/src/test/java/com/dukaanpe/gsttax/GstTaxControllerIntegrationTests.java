package com.dukaanpe.gsttax;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.gsttax.dto.CreateHsnRequest;
import com.dukaanpe.gsttax.dto.GenerateGstInvoiceRequest;
import com.dukaanpe.gsttax.dto.GstInvoiceItemRequest;
import com.dukaanpe.gsttax.dto.PrepareGstReturnRequest;
import com.dukaanpe.gsttax.dto.UpdateHsnRequest;
import com.dukaanpe.gsttax.entity.ReturnType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GstTaxControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldManageHsnGenerateInvoicePrepareReturnAndSummarizeTax() throws Exception {
        Long hsnId = createHsn("1001", "Test Product", "18.00", "0.00");

        mockMvc.perform(get("/api/gst/hsn/{id}", hsnId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.hsnCode").value("1001"))
            .andExpect(jsonPath("$.data.gstRate").value(18.00));

        Long invoiceId = createInvoice(301L, "GST-INV-001", LocalDate.now(), true, "1001", "1000.00");

        mockMvc.perform(get("/api/gst/invoices/{id}", invoiceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.invoiceNumber").value("GST-INV-001"))
            .andExpect(jsonPath("$.data.taxableAmount").value(1000.00))
            .andExpect(jsonPath("$.data.cgstAmount").value(90.00))
            .andExpect(jsonPath("$.data.sgstAmount").value(90.00))
            .andExpect(jsonPath("$.data.igstAmount").value(0.00))
            .andExpect(jsonPath("$.data.totalTaxAmount").value(180.00))
            .andExpect(jsonPath("$.data.invoiceTotal").value(1180.00));

        String today = LocalDate.now().toString();
        mockMvc.perform(get("/api/gst/invoices")
                .param("storeId", "301")
                .param("fromDate", today)
                .param("toDate", today)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));

        Long gstReturnId = prepareReturn(301L, ReturnType.GSTR1, LocalDate.now(), LocalDate.now());

        mockMvc.perform(get("/api/gst/returns/{id}", gstReturnId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.returnType").value("GSTR1"))
            .andExpect(jsonPath("$.data.totalInvoices").value(1))
            .andExpect(jsonPath("$.data.taxableAmount").value(1000.00))
            .andExpect(jsonPath("$.data.totalTaxAmount").value(180.00));

        mockMvc.perform(get("/api/tax/summary")
                .param("storeId", "301")
                .param("fromDate", today)
                .param("toDate", today))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.invoiceCount").value(1))
            .andExpect(jsonPath("$.data.totalTaxAmount").value(180.00))
            .andExpect(jsonPath("$.data.taxRateBreakup.length()").value(1))
            .andExpect(jsonPath("$.data.taxRateBreakup[0].gstRate").value(18.00));
    }

    @Test
    void shouldRejectInvoiceForInactiveHsnAndValidationEdges() throws Exception {
        Long hsnId = createHsn("2106", "Inactive Product", "12.00", "0.00");

        UpdateHsnRequest update = new UpdateHsnRequest();
        update.setDescription("Inactive Product");
        update.setGstRate(new BigDecimal("12.00"));
        update.setCessRate(new BigDecimal("0.00"));
        update.setActive(false);

        mockMvc.perform(put("/api/gst/hsn/{id}", hsnId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.active").value(false));

        GenerateGstInvoiceRequest request = invoiceRequest(302L, "GST-INV-002", LocalDate.now(), true, "2106", "500.00");
        mockMvc.perform(post("/api/gst/invoices/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/gst/hsn")
                .param("page", "0")
                .param("size", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(delete("/api/gst/hsn/{id}", hsnId))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldGenerateInterStateInvoiceUsingIgstOnly() throws Exception {
        createHsn("3304", "Interstate Product", "18.00", "0.00");

        Long invoiceId = createInvoice(303L, "GST-INV-IGST-001", LocalDate.now(), false, "3304", "1000.00");

        mockMvc.perform(get("/api/gst/invoices/{id}", invoiceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.cgstAmount").value(0.00))
            .andExpect(jsonPath("$.data.sgstAmount").value(0.00))
            .andExpect(jsonPath("$.data.igstAmount").value(180.00))
            .andExpect(jsonPath("$.data.totalTaxAmount").value(180.00))
            .andExpect(jsonPath("$.data.invoiceTotal").value(1180.00));
    }

    @Test
    void shouldCalculateCessHeavyInvoiceLine() throws Exception {
        createHsn("4401", "Cess Heavy Item", "12.00", "28.00");

        Long invoiceId = createInvoice(304L, "GST-INV-CESS-001", LocalDate.now(), true, "4401", "1000.00");

        mockMvc.perform(get("/api/gst/invoices/{id}", invoiceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.cgstAmount").value(60.00))
            .andExpect(jsonPath("$.data.sgstAmount").value(60.00))
            .andExpect(jsonPath("$.data.igstAmount").value(0.00))
            .andExpect(jsonPath("$.data.cessAmount").value(280.00))
            .andExpect(jsonPath("$.data.totalTaxAmount").value(400.00))
            .andExpect(jsonPath("$.data.invoiceTotal").value(1400.00));
    }

    @Test
    void shouldSummarizeMultiRateInvoicesWithRateWiseBreakup() throws Exception {
        createHsn("7001", "Low Rate Product", "5.00", "0.00");
        createHsn("7002", "High Rate Product", "18.00", "0.00");

        createInvoice(305L, "GST-INV-MR-001", LocalDate.now(), true, "7001", "1000.00");
        createInvoice(305L, "GST-INV-MR-002", LocalDate.now(), true, "7002", "1000.00");

        String today = LocalDate.now().toString();
        mockMvc.perform(get("/api/tax/summary")
                .param("storeId", "305")
                .param("fromDate", today)
                .param("toDate", today))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.invoiceCount").value(2))
            .andExpect(jsonPath("$.data.taxableAmount").value(2000.00))
            .andExpect(jsonPath("$.data.totalTaxAmount").value(230.00))
            .andExpect(jsonPath("$.data.taxRateBreakup.length()").value(2))
            .andExpect(jsonPath("$.data.taxRateBreakup[0].gstRate").value(5.00))
            .andExpect(jsonPath("$.data.taxRateBreakup[0].taxableAmount").value(1000.00))
            .andExpect(jsonPath("$.data.taxRateBreakup[0].totalTaxAmount").value(50.00))
            .andExpect(jsonPath("$.data.taxRateBreakup[1].gstRate").value(18.00))
            .andExpect(jsonPath("$.data.taxRateBreakup[1].taxableAmount").value(1000.00))
            .andExpect(jsonPath("$.data.taxRateBreakup[1].totalTaxAmount").value(180.00));
    }

    private Long createHsn(String hsnCode, String description, String gstRate, String cessRate) throws Exception {
        CreateHsnRequest request = new CreateHsnRequest();
        request.setHsnCode(hsnCode);
        request.setDescription(description);
        request.setGstRate(new BigDecimal(gstRate));
        request.setCessRate(new BigDecimal(cessRate));

        String response = mockMvc.perform(post("/api/gst/hsn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("id").asLong();
    }

    private Long createInvoice(Long storeId, String invoiceNumber, LocalDate invoiceDate, boolean intraState, String hsnCode, String taxableValue)
        throws Exception {
        GenerateGstInvoiceRequest request = invoiceRequest(storeId, invoiceNumber, invoiceDate, intraState, hsnCode, taxableValue);

        return createInvoice(request);
    }

    private Long createInvoice(GenerateGstInvoiceRequest request) throws Exception {

        String response = mockMvc.perform(post("/api/gst/invoices/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("id").asLong();
    }

    private Long prepareReturn(Long storeId, ReturnType returnType, LocalDate periodStart, LocalDate periodEnd) throws Exception {
        PrepareGstReturnRequest request = new PrepareGstReturnRequest();
        request.setStoreId(storeId);
        request.setReturnType(returnType);
        request.setPeriodStart(periodStart);
        request.setPeriodEnd(periodEnd);

        String response = mockMvc.perform(post("/api/gst/returns/prepare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("id").asLong();
    }

    private GenerateGstInvoiceRequest invoiceRequest(Long storeId, String invoiceNumber, LocalDate invoiceDate, boolean intraState,
                                                     String hsnCode, String taxableValue) {
        GstInvoiceItemRequest item = new GstInvoiceItemRequest();
        item.setHsnCode(hsnCode);
        item.setItemDescription("Taxable Line");
        item.setQuantity(new BigDecimal("1.000"));
        item.setTaxableValue(new BigDecimal(taxableValue));

        GenerateGstInvoiceRequest request = new GenerateGstInvoiceRequest();
        request.setStoreId(storeId);
        request.setInvoiceNumber(invoiceNumber);
        request.setInvoiceDate(invoiceDate);
        request.setCustomerName("GST Customer");
        request.setCustomerGstin("29ABCDE1234F2Z5");
        request.setPlaceOfSupply("KA");
        request.setIntraState(intraState);
        request.setItems(List.of(item));
        return request;
    }
}

