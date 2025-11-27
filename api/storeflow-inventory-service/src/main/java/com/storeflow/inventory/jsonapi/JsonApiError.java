package com.storeflow.inventory.jsonapi;

public record JsonApiError(
        String status,
        String title,
        String detail
) {
}
