package com.storeflow.products.jsonapi;

public record JsonApiResponse<T>(
        JsonApiData<T> data
) {
}
