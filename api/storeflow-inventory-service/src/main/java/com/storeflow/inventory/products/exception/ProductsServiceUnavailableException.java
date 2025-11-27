package com.storeflow.inventory.products.exception;

public class ProductsServiceUnavailableException extends RuntimeException {

    public ProductsServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductsServiceUnavailableException(String message) {
        super(message);
    }
}
