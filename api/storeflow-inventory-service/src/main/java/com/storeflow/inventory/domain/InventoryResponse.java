package com.storeflow.inventory.domain;

public record InventoryResponse(
        Long id,
        Long productId,
        Integer quantity
) {
}
