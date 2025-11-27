package com.storeflow.inventory.domain;

import com.storeflow.inventory.jsonapi.JsonApiData;
import com.storeflow.inventory.jsonapi.JsonApiListResponse;
import com.storeflow.inventory.jsonapi.JsonApiPaginationMeta;
import com.storeflow.inventory.jsonapi.JsonApiResponse;
import com.storeflow.inventory.products.ProductsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository repository;
    private final ProductsClient productsClient;

    public InventoryService(InventoryRepository repository, ProductsClient productsClient) {
        this.repository = repository;
        this.productsClient = productsClient;
    }

    public JsonApiResponse<InventoryResponse> createOrUpdate(InventoryRequest request) {

        productsClient.validateProductExists(request.productId());

        Inventory inventory = repository.findByProductId(request.productId())
                .orElseGet(Inventory::new);

        Integer previousQuantity = inventory.getId() == null ? null : inventory.getQuantity();

        inventory.setProductId(request.productId());
        inventory.setQuantity(request.quantity());

        Inventory saved = repository.save(inventory);

        log.info(
                "InventoryChangedEvent productId={} previousQuantity={} newQuantity={}",
                saved.getProductId(),
                previousQuantity,
                saved.getQuantity()
        );

        InventoryResponse response =
                new InventoryResponse(saved.getId(), saved.getProductId(), saved.getQuantity());

        JsonApiData<InventoryResponse> data =
                new JsonApiData<>("inventories", String.valueOf(saved.getId()), response);

        return new JsonApiResponse<>(data);
    }

    public JsonApiResponse<InventoryResponse> findByProductId(Long productId) {
        Inventory inventory = repository.findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException("Inventory for product " + productId + " not found"));

        InventoryResponse response =
                new InventoryResponse(inventory.getId(), inventory.getProductId(), inventory.getQuantity());

        JsonApiData<InventoryResponse> data =
                new JsonApiData<>("inventories", String.valueOf(inventory.getId()), response);

        return new JsonApiResponse<>(data);
    }

    public JsonApiListResponse<InventoryResponse> findPage(int pageNumber, int pageSize) {
        Page<Inventory> page = repository.findAll(PageRequest.of(pageNumber, pageSize));

        var data = page.getContent().stream()
                .map(inv -> new JsonApiData<>(
                        "inventories",
                        String.valueOf(inv.getId()),
                        new InventoryResponse(inv.getId(), inv.getProductId(), inv.getQuantity())
                ))
                .toList();

        JsonApiPaginationMeta meta = new JsonApiPaginationMeta(
                pageNumber,
                pageSize,
                page.getTotalElements(),
                page.getTotalPages()
        );

        return new JsonApiListResponse<>(data, meta);
    }

    public JsonApiResponse<InventoryResponse> purchase(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        productsClient.validateProductExists(productId);

        Inventory inventory = repository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory for product " + productId + " not found"));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock for product " + productId);
        }

        int previousQuantity = inventory.getQuantity();
        int newQuantity = previousQuantity - quantity;
        inventory.setQuantity(newQuantity);

        Inventory saved = repository.save(inventory);

        log.info(
                "InventoryChangedEvent productId={} previousQuantity={} newQuantity={} purchasedQuantity={}",
                saved.getProductId(),
                previousQuantity,
                saved.getQuantity(),
                quantity
        );

        InventoryResponse response =
                new InventoryResponse(saved.getId(), saved.getProductId(), saved.getQuantity());

        JsonApiData<InventoryResponse> data =
                new JsonApiData<>("inventories", String.valueOf(saved.getId()), response);

        return new JsonApiResponse<>(data);
    }
}
