package com.storeflow.products.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storeflow.products.jsonapi.JsonApiData;
import com.storeflow.products.jsonapi.JsonApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void create_shouldReturn201AndJsonApiBody() throws Exception {
        ProductRequest request = new ProductRequest("Monitor 27\"", new BigDecimal("950.0"));

        ProductResponse responseAttributes = new ProductResponse(
                1L,
                "Monitor 27\"",
                new BigDecimal("950.0")
        );

        JsonApiData<ProductResponse> data = new JsonApiData<>(
                "products",
                "1",
                responseAttributes
        );

        JsonApiResponse<ProductResponse> serviceResponse = new JsonApiResponse<>(data);

        when(productService.create(any(ProductRequest.class))).thenReturn(serviceResponse);

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.type").value("products"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.attributes.id").value(1))
                .andExpect(jsonPath("$.data.attributes.name").value("Monitor 27\""))
                .andExpect(jsonPath("$.data.attributes.price").value(950.0));
    }
}
