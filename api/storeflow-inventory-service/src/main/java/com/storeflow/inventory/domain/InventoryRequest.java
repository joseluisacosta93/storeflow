package com.storeflow.inventory.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryRequest(
        @NotNull Long productId,
        @NotNull @Min(0) Integer quantity
) {
}
