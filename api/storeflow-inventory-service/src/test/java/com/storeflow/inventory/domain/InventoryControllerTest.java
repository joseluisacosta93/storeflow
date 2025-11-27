package com.storeflow.inventory.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storeflow.inventory.jsonapi.JsonApiData;
import com.storeflow.inventory.jsonapi.JsonApiListResponse;
import com.storeflow.inventory.jsonapi.JsonApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void createOrUpdate_shouldReturn201AndJsonApiBody() throws Exception {
        InventoryRequest request = new InventoryRequest(1L, 10);

        InventoryResponse responseAttributes = new InventoryResponse(
                5L,
                1L,
                10
        );

        JsonApiData<InventoryResponse> data = new JsonApiData<>(
                "inventories",
                "5",
                responseAttributes
        );

        JsonApiResponse<InventoryResponse> serviceResponse =
                new JsonApiResponse<>(data);

        when(inventoryService.createOrUpdate(any(InventoryRequest.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(
                        post("/api/v1/inventories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.type").value("inventories"))
                .andExpect(jsonPath("$.data.id").value("5"))
                .andExpect(jsonPath("$.data.attributes.id").value(5))
                .andExpect(jsonPath("$.data.attributes.productId").value(1))
                .andExpect(jsonPath("$.data.attributes.quantity").value(10));
    }

    @Test
    void findByProductId_shouldReturn200AndJsonApiBody() throws Exception {
        InventoryResponse responseAttributes = new InventoryResponse(
                3L,
                10L,
                7
        );

        JsonApiData<InventoryResponse> data = new JsonApiData<>(
                "inventories",
                "3",
                responseAttributes
        );

        JsonApiResponse<InventoryResponse> serviceResponse =
                new JsonApiResponse<>(data);

        when(inventoryService.findByProductId(10L))
                .thenReturn(serviceResponse);

        mockMvc.perform(
                        get("/api/v1/inventories")
                                .param("productId", "10")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("inventories"))
                .andExpect(jsonPath("$.data.id").value("3"))
                .andExpect(jsonPath("$.data.attributes.productId").value(10))
                .andExpect(jsonPath("$.data.attributes.quantity").value(7));
    }

    @Test
    void findPage_shouldReturn200AndJsonApiListResponse() throws Exception {
        InventoryResponse firstInventory = new InventoryResponse(
                1L,
                1L,
                5
        );

        InventoryResponse secondInventory = new InventoryResponse(
                2L,
                2L,
                8
        );

        JsonApiData<InventoryResponse> firstData = new JsonApiData<>(
                "inventories",
                "1",
                firstInventory
        );

        JsonApiData<InventoryResponse> secondData = new JsonApiData<>(
                "inventories",
                "2",
                secondInventory
        );

        JsonApiListResponse<InventoryResponse> serviceResponse =
                new JsonApiListResponse<>(
                        List.of(firstData, secondData),
                        null
                );

        when(inventoryService.findPage(0, 10))
                .thenReturn(serviceResponse);

        mockMvc.perform(
                        get("/api/v1/inventories/page")
                                .param("page[number]", "0")
                                .param("page[size]", "10")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("inventories"))
                .andExpect(jsonPath("$.data[0].id").value("1"))
                .andExpect(jsonPath("$.data[0].attributes.productId").value(1))
                .andExpect(jsonPath("$.data[0].attributes.quantity").value(5))
                .andExpect(jsonPath("$.data[1].id").value("2"));
    }
}
