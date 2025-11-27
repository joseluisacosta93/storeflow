package com.storeflow.inventory.jsonapi;

import java.util.List;

public record JsonApiErrorResponse(
        List<JsonApiError> errors
) {
}
