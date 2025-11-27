package com.storeflow.inventory.products;

import com.storeflow.inventory.products.exception.ProductsServiceException;
import com.storeflow.inventory.products.exception.ProductsServiceUnavailableException;
import com.storeflow.inventory.products.exception.RemoteProductNotFoundException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductsClient {

    private final RestTemplate restTemplate;
    private final ProductsServiceProperties properties;

    private static final int MAX_ATTEMPTS = 3;

    public ProductsClient(RestTemplate productsRestTemplate,
                          ProductsServiceProperties properties) {
        this.restTemplate = productsRestTemplate;
        this.properties = properties;
    }

    public void validateProductExists(Long productId) {
        assertProductExists(productId);
    }

    public void assertProductExists(Long productId) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                doRequest(productId);
                return;
            } catch (ResourceAccessException ex) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw new ProductsServiceUnavailableException(
                            "Error contacting products-service after " + MAX_ATTEMPTS + " attempts",
                            ex
                    );
                }
                sleepBackoff(attempt);
            }
        }
    }

    private void doRequest(Long productId) {
        String url = properties.getBaseUrl() + "/api/v1/products/" + productId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", properties.getApiKey());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            int status = response.getStatusCode().value();

            if (status == 404) {
                throw new RemoteProductNotFoundException(productId);
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ProductsServiceException(
                        "Unexpected status calling products-service: " + status
                );
            }
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RemoteProductNotFoundException(productId);
            }
            throw ex;
        }
    }

    private void sleepBackoff(int attempt) {
        long delayMs = 100L * attempt;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProductsServiceUnavailableException(
                    "Products-service retry interrupted", e
            );
        }
    }
}
