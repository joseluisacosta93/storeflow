package com.storeflow.products.jsonapi;

import java.util.List;

public record JsonApiListResponse<T>(
        List<JsonApiData<T>> data,
        JsonApiPaginationMeta meta
) {
}
