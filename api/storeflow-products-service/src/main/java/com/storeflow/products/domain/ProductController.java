package com.storeflow.products.domain;

import com.storeflow.products.jsonapi.JsonApiListResponse;
import com.storeflow.products.jsonapi.JsonApiResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Products", description = "Operations related to product management")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(
            summary = "Create product",
            description = "Creates a new product and returns it wrapped in a JSON:API response."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(schema = @Schema(implementation = JsonApiResponse.class))
    )
    @PostMapping
    public ResponseEntity<JsonApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest request
    ) {
        JsonApiResponse<ProductResponse> response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get product by id",
            description = "Returns a single product wrapped in a JSON:API response."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(schema = @Schema(implementation = JsonApiResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Product not found"
    )
    @GetMapping("/{id}")
    public ResponseEntity<JsonApiResponse<ProductResponse>> findById(
            @Parameter(description = "Product identifier", example = "1")
            @PathVariable Long id
    ) {
        JsonApiResponse<ProductResponse> response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "List products",
            description = "Returns a paginated list of products in JSON:API format."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Products page retrieved successfully",
            content = @Content(schema = @Schema(implementation = JsonApiListResponse.class))
    )
    @GetMapping
    public ResponseEntity<JsonApiListResponse<ProductResponse>> findPage(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page[number]", defaultValue = "0") int pageNumber,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "page[size]", defaultValue = "10") int pageSize
    ) {
        JsonApiListResponse<ProductResponse> response = service.findPage(pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update product",
            description = "Updates an existing product and returns it wrapped in a JSON:API response."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully",
            content = @Content(schema = @Schema(implementation = JsonApiResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Product not found"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<JsonApiResponse<ProductResponse>> update(
            @Parameter(description = "Product identifier", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        JsonApiResponse<ProductResponse> response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete product",
            description = "Deletes a product by id."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Product deleted successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Product not found"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Product identifier", example = "1")
            @PathVariable Long id
    ) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
