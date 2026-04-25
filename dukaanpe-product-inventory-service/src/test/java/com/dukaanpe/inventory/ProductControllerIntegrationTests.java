package com.dukaanpe.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.inventory.dto.ProductRequest;
import com.dukaanpe.inventory.entity.ProductUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListSeededCategories() throws Exception {
        mockMvc.perform(get("/api/products/categories").param("storeId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(6));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setStoreId(1L);
        request.setCategoryId(1L);
        request.setProductName("Test Product");
        request.setSellingPrice(new java.math.BigDecimal("99"));
        request.setPurchasePrice(new java.math.BigDecimal("90"));
        request.setUnit(ProductUnit.PIECE);
        request.setUnitQuantity(1.0);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.productName").value("Test Product"));
    }

    @Test
    void shouldApplySearchFiltersWithPagination() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("storeId", "1")
                .param("q", "milk")
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.pageSize").value(5))
            .andExpect(jsonPath("$.data.content.length()").isNumber());
    }

    @Test
    void shouldApplyCategoryAndQueryTogether() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("storeId", "1")
                .param("q", "milk")
                .param("categoryId", "1")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].productName").value("Amul Taza Milk"));
    }

    @Test
    void shouldReturnEmptyWhenCategoryAndQueryDoNotMatch() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("storeId", "1")
                .param("q", "milk")
                .param("categoryId", "2")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void shouldHandlePaginationBoundariesForSearch() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("storeId", "1")
                .param("q", "a")
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/products/search")
                .param("storeId", "1")
                .param("q", "oil")
                .param("page", "1")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.pageNumber").value(1));
    }

    @Test
    void shouldRejectInvalidPaginationForList() throws Exception {
        mockMvc.perform(get("/api/products")
                .param("storeId", "1")
                .param("page", "-1")
                .param("size", "1000"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}

