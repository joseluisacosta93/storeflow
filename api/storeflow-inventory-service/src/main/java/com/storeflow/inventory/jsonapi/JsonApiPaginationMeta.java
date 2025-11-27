package com.storeflow.inventory.jsonapi;

public record JsonApiPaginationMeta(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {
}
