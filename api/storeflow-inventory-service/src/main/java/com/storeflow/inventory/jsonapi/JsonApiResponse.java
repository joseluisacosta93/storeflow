package com.storeflow.inventory.jsonapi;

public record JsonApiResponse<T>(
        JsonApiData<T> data
) {
}
