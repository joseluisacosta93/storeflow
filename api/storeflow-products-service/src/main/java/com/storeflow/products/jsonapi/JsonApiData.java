package com.storeflow.products.jsonapi;

public record JsonApiData<T>(
        String type,
        String id,
        T attributes
) {
}
