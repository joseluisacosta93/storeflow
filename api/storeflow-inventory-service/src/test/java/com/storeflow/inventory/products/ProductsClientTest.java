package com.storeflow.inventory.products;

import com.storeflow.inventory.products.exception.ProductsServiceUnavailableException;
import com.storeflow.inventory.products.exception.RemoteProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsClientTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    ProductsServiceProperties properties;

    ProductsClient client;

    @BeforeEach
    void setUp() {
        client = new ProductsClient(restTemplate, properties);
    }

    @Test
    void assertProductExists_okResponse_doesNotThrow() {
        when(properties.getBaseUrl()).thenReturn("http://products-service:8081");
        when(properties.getApiKey()).thenReturn("test-api-key");

        ResponseEntity<String> response =
                new ResponseEntity<>("{\"data\":{\"id\":\"1\"}}", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        assertDoesNotThrow(() -> client.assertProductExists(1L));

        verify(restTemplate, times(1)).exchange(
                contains("/api/v1/products/1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void assertProductExists_notFound_throwsRemoteProductNotFoundException() {
        when(properties.getBaseUrl()).thenReturn("http://products-service:8081");
        when(properties.getApiKey()).thenReturn("test-api-key");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(RemoteProductNotFoundException.class,
                () -> client.assertProductExists(99L));
    }

    @Test
    void assertProductExists_retriesOnResourceAccessException_thenSucceeds() {
        when(properties.getBaseUrl()).thenReturn("http://products-service:8081");
        when(properties.getApiKey()).thenReturn("test-api-key");

        ResponseEntity<String> okResponse =
                new ResponseEntity<>("{\"data\":{\"id\":\"1\"}}", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        ))
                .thenThrow(new ResourceAccessException("timeout"))
                .thenReturn(okResponse);

        assertDoesNotThrow(() -> client.assertProductExists(1L));

        verify(restTemplate, times(2)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void assertProductExists_threeResourceAccessExceptions_throwsUnavailable() {
        when(properties.getBaseUrl()).thenReturn("http://products-service:8081");
        when(properties.getApiKey()).thenReturn("test-api-key");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new ResourceAccessException("timeout"));

        assertThrows(ProductsServiceUnavailableException.class,
                () -> client.assertProductExists(1L));

        verify(restTemplate, times(3)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}
