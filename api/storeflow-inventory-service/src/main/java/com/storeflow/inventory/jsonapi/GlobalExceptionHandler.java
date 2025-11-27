package com.storeflow.inventory.jsonapi;

import com.storeflow.inventory.domain.InventoryNotFoundException;
import com.storeflow.inventory.products.exception.RemoteProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<JsonApiErrorResponse> handleInventoryNotFound(InventoryNotFoundException ex) {
        JsonApiError error = new JsonApiError(
                String.valueOf(HttpStatus.NOT_FOUND.value()),
                "Inventory not found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new JsonApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        JsonApiError error = new JsonApiError(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "Validation error",
                detail
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JsonApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(RemoteProductNotFoundException.class)
    public ResponseEntity<JsonApiErrorResponse> handleRemoteProductNotFound(RemoteProductNotFoundException ex) {
        JsonApiError error = new JsonApiError(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "Product does not exist",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JsonApiErrorResponse(List.of(error)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiErrorResponse> handleGeneric(Exception ex) {
        JsonApiError error = new JsonApiError(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                "Internal server error",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonApiErrorResponse(List.of(error)));
    }
}
