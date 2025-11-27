package com.storeflow.inventory.domain;

import com.storeflow.inventory.jsonapi.JsonApiListResponse;
import com.storeflow.inventory.jsonapi.JsonApiResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inventory", description = "Operations related to inventory management")
@RestController
@RequestMapping("/api/v1/inventories")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @Operation(
            summary = "Create or update inventory",
            description = "Creates or updates the inventory record for a given product and returns it in JSON:API format."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Inventory created or updated successfully",
            content = @Content(schema = @Schema(implementation = JsonApiResponse.class))
    )
    @PostMapping
    public ResponseEntity<JsonApiResponse<InventoryResponse>> createOrUpdate(
            @Valid @RequestBody InventoryRequest request
    ) {
        JsonApiResponse<InventoryResponse> response = service.createOrUpdate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get inventory by product id",
            description = "Returns the inventory record for the given product id wrapped in a JSON:API response."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Inventory retrieved successfully",
            content = @Content(schema = @Schema(implementation = JsonApiResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Inventory or product not found"
    )
    @GetMapping
    public ResponseEntity<JsonApiResponse<InventoryResponse>> findByProductId(
            @Parameter(description = "Product identifier", example = "1")
            @RequestParam(name = "productId") Long productId
    ) {
        JsonApiResponse<InventoryResponse> response = service.findByProductId(productId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "List inventories",
            description = "Returns a paginated list of inventories in JSON:API format."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Inventories page retrieved successfully",
            content = @Content(schema = @Schema(implementation = JsonApiListResponse.class))
    )
    @GetMapping("/page")
    public ResponseEntity<JsonApiListResponse<InventoryResponse>> findPage(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page[number]", defaultValue = "0") int pageNumber,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "page[size]", defaultValue = "10") int pageSize
    ) {
        JsonApiListResponse<InventoryResponse> response = service.findPage(pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Register purchase",
            description = "Registers a purchase for a given product, decreases stock and returns the updated inventory."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Purchase registered and inventory updated",
            content = @Content(schema = @Schema(implementation = JsonApiResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Not enough stock to process the purchase"
    )
    @PostMapping("/purchase")
    public ResponseEntity<JsonApiResponse<InventoryResponse>> purchase(
            @Valid @RequestBody PurchaseInventoryRequest request
    ) {
        JsonApiResponse<InventoryResponse> response =
                service.purchase(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(response);
    }
}
