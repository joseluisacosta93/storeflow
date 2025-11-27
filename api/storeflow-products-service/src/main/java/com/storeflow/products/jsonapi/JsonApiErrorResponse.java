package com.storeflow.products.jsonapi;

import java.util.List;

public record JsonApiErrorResponse(
        List<JsonApiError> errors
) {
}
