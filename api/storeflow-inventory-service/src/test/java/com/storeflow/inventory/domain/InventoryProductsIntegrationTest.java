package com.storeflow.inventory.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:inventory_it_products;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "security.api-key=secret-public-api-key",
        "products-service.base-url=http://localhost:8081",
        "products-service.api-key=secret-public-api-key",
        "products-service.connect-timeout-ms=2000",
        "products-service.read-timeout-ms=2000"
})
@EnabledIfEnvironmentVariable(named = "IT_WITH_PRODUCTS", matches = "true")
class InventoryProductsIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createInventoryAndPurchaseUsingRealProductsService() throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders productHeaders = new HttpHeaders();
        productHeaders.setContentType(MediaType.APPLICATION_JSON);
        productHeaders.set("X-API-KEY", "secret-public-api-key");

        String productBody = """
                {
                  "name": "Integration Product",
                  "price": 15.50
                }
                """;

        HttpEntity<String> productRequest = new HttpEntity<>(productBody, productHeaders);

        ResponseEntity<String> productResponse = restTemplate.postForEntity(
                "http://localhost:8081/api/v1/products",
                productRequest,
                String.class
        );

        assertEquals(HttpStatus.CREATED, productResponse.getStatusCode());

        JsonNode root = objectMapper.readTree(productResponse.getBody());
        long productId = root.path("data").path("id").asLong();

        String createInventoryBody = """
                {
                  "productId": %d,
                  "quantity": 10
                }
                """.formatted(productId);

        mockMvc.perform(
                        post("/api/v1/inventories")
                                .header("X-API-KEY", "secret-public-api-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createInventoryBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.attributes.productId").value((int) productId))
                .andExpect(jsonPath("$.data.attributes.quantity").value(10));

        String purchaseBody = """
                {
                  "productId": %d,
                  "quantity": 3
                }
                """.formatted(productId);

        mockMvc.perform(
                        post("/api/v1/inventories/purchase")
                                .header("X-API-KEY", "secret-public-api-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(purchaseBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.productId").value((int) productId))
                .andExpect(jsonPath("$.data.attributes.quantity").value(7));

        mockMvc.perform(
                        get("/api/v1/inventories")
                                .header("X-API-KEY", "secret-public-api-key")
                                .param("productId", String.valueOf(productId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.productId").value((int) productId))
                .andExpect(jsonPath("$.data.attributes.quantity").value(7));
    }
}
