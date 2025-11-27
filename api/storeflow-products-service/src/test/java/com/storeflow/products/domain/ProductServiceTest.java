package com.storeflow.products.domain;


import com.storeflow.products.domain.Product;
import com.storeflow.products.domain.ProductNotFoundException;
import com.storeflow.products.domain.ProductRepository;
import com.storeflow.products.domain.ProductRequest;
import com.storeflow.products.domain.ProductResponse;
import com.storeflow.products.domain.ProductService;
import com.storeflow.products.jsonapi.JsonApiData;
import com.storeflow.products.jsonapi.JsonApiListResponse;
import com.storeflow.products.jsonapi.JsonApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    void create_shouldPersistProductAndReturnJsonApiResponse() {
        ProductRequest request = new ProductRequest("Monitor 27\"", new BigDecimal("950.0"));

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Monitor 27\"");
        saved.setPrice(new BigDecimal("950.0"));

        when(repository.save(any(Product.class))).thenReturn(saved);

        JsonApiResponse<ProductResponse> response = service.create(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(productCaptor.capture());

        Product productToSave = productCaptor.getValue();
        assertEquals("Monitor 27\"", productToSave.getName());
        assertEquals(new BigDecimal("950.0"), productToSave.getPrice());

        assertNotNull(response);
        assertNotNull(response.data());
        assertEquals("products", response.data().type());
        assertEquals("1", response.data().id());

        ProductResponse attributes = response.data().attributes();
        assertEquals(1L, attributes.id());
        assertEquals("Monitor 27\"", attributes.name());
        assertEquals(new BigDecimal("950.0"), attributes.price());
    }

    @Test
    void findById_shouldReturnProductWhenExists() {
        Product product = new Product();
        product.setId(2L);
        product.setName("Teclado mecánico");
        product.setPrice(new BigDecimal("300.0"));

        when(repository.findById(2L)).thenReturn(Optional.of(product));

        JsonApiResponse<ProductResponse> response = service.findById(2L);

        assertNotNull(response.data());
        assertEquals("products", response.data().type());
        assertEquals("2", response.data().id());
        assertEquals("Teclado mecánico", response.data().attributes().name());
        assertEquals(new BigDecimal("300.0"), response.data().attributes().price());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void findPage_shouldReturnPagedJsonApiListResponse() {
        Product firstProduct = new Product();
        firstProduct.setId(1L);
        firstProduct.setName("Monitor 24\"");
        firstProduct.setPrice(new BigDecimal("100.0"));

        Product secondProduct = new Product();
        secondProduct.setId(2L);
        secondProduct.setName("Teclado mecánico");
        secondProduct.setPrice(new BigDecimal("200.0"));

        int pageNumber = 0;
        int pageSize = 10;

        Page<Product> productPage = new PageImpl<>(
                List.of(firstProduct, secondProduct),
                PageRequest.of(pageNumber, pageSize),
                2
        );

        when(repository.findAll(PageRequest.of(pageNumber, pageSize)))
                .thenReturn(productPage);

        JsonApiListResponse<ProductResponse> response = service.findPage(pageNumber, pageSize);

        assertNotNull(response);
        assertNotNull(response.data());
        assertEquals(2, response.data().size());

        JsonApiData<ProductResponse> firstItem = response.data().get(0);
        assertEquals("products", firstItem.type());
        assertEquals("1", firstItem.id());
        assertEquals("Monitor 24\"", firstItem.attributes().name());
    }
}
