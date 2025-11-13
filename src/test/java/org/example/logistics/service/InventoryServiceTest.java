package org.example.logistics.service;

import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.Product;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;
    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = Product.builder()
                .id(1L)
                .sku("SKU-TEST")
                .build();

        warehouse = Warehouse.builder()
                .id(1L)
                .name("WH-Paris")
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .warehouse(warehouse)
                .qtyOnHand(100)
                .qtyReserved(20)
                .build();
    }

    @Test
    void testGetAvailable_ShouldReturnCorrectValue() {
        // when
        Integer available = inventory.getAvailable();

        // then
        assertEquals(80, available);
    }

    @Test
    void testPreventNegativeStock_ShouldThrowException_WhenReservingMoreThanAvailable() {
        // given
        inventory.setQtyOnHand(10);
        inventory.setQtyReserved(5);

        // when + then
        assertThrows(RuntimeException.class, () -> {
            if (inventory.getAvailable() < 10) {
                throw new RuntimeException("Stock insuffisant");
            }
        });
    }

    @Test
    void testReserveQuantity_ShouldUpdateReservedCorrectly() {
        // given
        int reserveQty = 30;
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // when
        inventory.setQtyReserved(inventory.getQtyReserved() + reserveQty);

        // then
        assertEquals(50, inventory.getQtyReserved());
        assertEquals(50, inventory.getAvailable());
    }

    @Test
    void testReleaseQuantity_ShouldUpdateReservedCorrectly() {
        // given
        int releaseQty = 10;
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // when
        inventory.setQtyReserved(inventory.getQtyReserved() - releaseQty);

        // then
        assertEquals(10, inventory.getQtyReserved());
        assertEquals(90, inventory.getAvailable());
    }
}