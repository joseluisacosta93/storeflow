package com.storeflow.inventory.domain;

import com.storeflow.inventory.products.ProductsClient;
import com.storeflow.inventory.products.exception.RemoteProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    InventoryRepository inventoryRepository;

    @Mock
    ProductsClient productsClient;

    InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(inventoryRepository, productsClient);
    }

    @Test
    void purchaseValidProductDoesNotThrow() {
        Inventory inventory = mock(Inventory.class);
        when(inventory.getQuantity()).thenReturn(10);
        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(inventory);

        assertDoesNotThrow(() -> service.purchase(1L, 2));

        verify(productsClient).validateProductExists(1L);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void purchaseInvalidProductThrowsRemoteProductNotFoundException() {
        doThrow(new RemoteProductNotFoundException(99L))
                .when(productsClient).validateProductExists(99L);

        assertThrows(RemoteProductNotFoundException.class,
                () -> service.purchase(99L, 1));
    }
}
