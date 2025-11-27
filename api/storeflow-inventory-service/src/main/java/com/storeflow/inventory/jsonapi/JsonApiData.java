package com.storeflow.inventory.jsonapi;

public record JsonApiData<T>(
        String type,
        String id,
        T attributes
) {
}
