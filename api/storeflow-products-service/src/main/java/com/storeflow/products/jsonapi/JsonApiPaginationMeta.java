package com.storeflow.products.jsonapi;

public record JsonApiPaginationMeta(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {
}
