package com.storeflow.products.jsonapi;

public record JsonApiError(
        String status,
        String title,
        String detail
) {
}
