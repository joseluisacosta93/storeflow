package com.storeflow.products.domain;
import java.math.BigDecimal;



public record ProductResponse(
        Long id,
        String name,
        BigDecimal price
) {
}
