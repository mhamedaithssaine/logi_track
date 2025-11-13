package org.example.logistics.service.shipment;

import org.example.logistics.dto.order.SalesOrderReserveDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.Product;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.OrderReserveMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.service.OrderReserveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderReserveServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderReserveMapper orderReserveMapper;

    @InjectMocks
    private OrderReserveService orderReserveService;

    private Product product;
    private Warehouse warehouse;
    private SalesOrderLine line;
    private SalesOrder order;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = Product.builder()
                .id(1L)
                .sku("SKU-TEST")
                .name("Produit Test")
                .build();

        warehouse = Warehouse.builder()
                .id(1L)
                .code("WH-001")
                .name("WH-Paris")
                .build();

        line = SalesOrderLine.builder()
                .id(10L)
                .product(product)
                .quantity(10)
                .backorderQty(0)
                .build();

        order = SalesOrder.builder()
                .id(100L)
                .status(Status.CREATED)
                .warehouse(warehouse)
                .lines(Collections.singletonList(line))
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .warehouse(warehouse)
                .qtyOnHand(0)
                .qtyReserved(0)
                .build();
    }

    @Test
    void shouldCreateBackorder_whenStockIsZero() {
        // given
        inventory.setQtyOnHand(0);
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .thenReturn(Optional.of(inventory));
        when(orderReserveMapper.toDto(any())).thenReturn(null);

        // when
        orderReserveService.reserveOrder(dto);

        // then
        assertEquals(Status.PARTIAL_RESERVED, order.getStatus());
        assertEquals(0, inventory.getQtyReserved());
        assertEquals(10, line.getBackorderQty());
        verify(inventoryRepository).save(any());
    }

    @Test
    void shouldAllowPartialReservation_whenStockAvailableButNotEnough() {
        // given
        inventory.setQtyOnHand(5);
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .thenReturn(Optional.of(inventory));
        when(orderReserveMapper.toDto(any())).thenReturn(null);

        // when
        orderReserveService.reserveOrder(dto);

        // then
        assertEquals(Status.PARTIAL_RESERVED, order.getStatus());
        assertEquals(5, inventory.getQtyReserved());
        assertEquals(5, line.getBackorderQty());
        verify(inventoryRepository).save(any());
    }

    @Test
    void shouldReserveCompletely_whenStockIsSufficient() {
        // given
        inventory.setQtyOnHand(15);
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .thenReturn(Optional.of(inventory));
        when(orderReserveMapper.toDto(any())).thenReturn(null);

        // when
        orderReserveService.reserveOrder(dto);

        // then
        assertEquals(Status.RESERVED, order.getStatus());
        assertEquals(10, inventory.getQtyReserved());
        assertEquals(0, line.getBackorderQty());
        verify(inventoryRepository).save(any());
    }

    @Test
    void shouldThrow_whenOrderNotFound() {
        // given
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(999L)
                .build();
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderReserveService.reserveOrder(dto));
        assertTrue(ex.getMessage().contains("Commande non trouvée"));
    }

    @Test
    void shouldThrow_whenOrderNotCreated() {
        // given
        order.setStatus(Status.RESERVED);
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        // when + then
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> orderReserveService.reserveOrder(dto));
        assertTrue(ex.getMessage().contains("Impossible de réserver"));
    }

    @Test
    void shouldThrow_whenInventoryNotFound() {
        // given
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();
        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .thenReturn(Optional.empty());

        // when + then
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderReserveService.reserveOrder(dto));
        assertTrue(ex.getMessage().contains("Inventaire non trouvé"));
    }
}
