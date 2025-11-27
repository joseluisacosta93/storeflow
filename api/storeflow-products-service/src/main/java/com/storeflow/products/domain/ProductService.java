package com.storeflow.products.domain;

import com.storeflow.products.jsonapi.JsonApiData;
import com.storeflow.products.jsonapi.JsonApiListResponse;
import com.storeflow.products.jsonapi.JsonApiPaginationMeta;
import com.storeflow.products.jsonapi.JsonApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public JsonApiResponse<ProductResponse> create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());

        Product saved = repository.save(product);

        ProductResponse response = new ProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getPrice()
        );

        JsonApiData<ProductResponse> data = new JsonApiData<>(
                "products",
                String.valueOf(saved.getId()),
                response
        );

        return new JsonApiResponse<>(data);
    }

    public JsonApiResponse<ProductResponse> findById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice()
        );

        JsonApiData<ProductResponse> data = new JsonApiData<>(
                "products",
                String.valueOf(product.getId()),
                response
        );

        return new JsonApiResponse<>(data);
    }

    public JsonApiListResponse<ProductResponse> findPage(int pageNumber, int pageSize) {
        Page<Product> page = repository.findAll(PageRequest.of(pageNumber, pageSize));

        var data = page.getContent().stream()
                .map(product -> new JsonApiData<>(
                        "products",
                        String.valueOf(product.getId()),
                        new ProductResponse(
                                product.getId(),
                                product.getName(),
                                product.getPrice()
                        )
                ))
                .toList();

        JsonApiPaginationMeta meta = new JsonApiPaginationMeta(
                pageNumber,
                pageSize,
                page.getTotalElements(),
                page.getTotalPages()
        );

        return new JsonApiListResponse<>(data, meta);
    }

    public JsonApiResponse<ProductResponse> update(Long id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name());
        product.setPrice(request.price());

        Product saved = repository.save(product);

        ProductResponse response = new ProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getPrice()
        );

        JsonApiData<ProductResponse> data = new JsonApiData<>(
                "products",
                String.valueOf(saved.getId()),
                response
        );

        return new JsonApiResponse<>(data);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
