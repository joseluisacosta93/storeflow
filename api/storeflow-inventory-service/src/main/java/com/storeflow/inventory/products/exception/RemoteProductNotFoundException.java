package com.storeflow.inventory.products.exception;

public class RemoteProductNotFoundException extends RuntimeException {

    public RemoteProductNotFoundException(Long productId) {
        super("Product " + productId + " not found in products-service");
    }
}
