package org.example.logistics;

import org.example.logistics.dto.order.SalesOrderReserveDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.Product;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.example.logistics.entity.Warehouse;
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
                .name("WH-Paris")
                .build();

        line = SalesOrderLine.builder()
                .product(product)
                .quantity(10)
                .build();

        order = SalesOrder.builder()
                .id(100L)
                .status(Status.CREATED)
                .warehouse(warehouse)
                .lines(Collections.singletonList(line))
                .build();

        inventory = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .qtyReserved(0)
                .build();
    }

    @Test
    void testReserveOrder_ShouldCreateBackorder_WhenStockIsZero() {
        // given
        inventory.setQtyOnHand(0);
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .thenReturn(Optional.of(inventory));
        when(orderReserveMapper.toDto(any(SalesOrder.class)))
                .thenReturn(null);

        // when
        orderReserveService.reserveOrder(dto);

        // then
        assertEquals(Status.PARTIAL_RESERVED, order.getStatus());
        assertEquals(0, inventory.getQtyReserved());
        assertEquals(10, line.getBackorderQty());
        verify(inventoryRepository, times(1)).save(any());
    }

    @Test
    void testReserveOrder_ShouldAllowPartialReservation_WhenStockAvailableButNotEnough() {
        // given
        inventory.setQtyOnHand(5);
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .thenReturn(Optional.of(inventory));
        when(orderReserveMapper.toDto(any(SalesOrder.class)))
                .thenReturn(null);

        // when
        orderReserveService.reserveOrder(dto);

        // then
        assertEquals(Status.PARTIAL_RESERVED, order.getStatus());
        assertEquals(5, inventory.getQtyReserved());
        assertEquals(5, line.getBackorderQty());
        verify(inventoryRepository, times(1)).save(any());
    }

    @Test
    void testReserveOrder_ShouldReserveCompletely_WhenStockSufficient() {
        // given
        inventory.setQtyOnHand(15);
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .thenReturn(Optional.of(inventory));
        when(orderReserveMapper.toDto(any(SalesOrder.class)))
                .thenReturn(null);

        // when
        orderReserveService.reserveOrder(dto);

        // then
        assertEquals(Status.RESERVED, order.getStatus());
        assertEquals(10, inventory.getQtyReserved());
        assertEquals(0, line.getBackorderQty());
        verify(inventoryRepository, times(1)).save(any());
    }

    @Test
    void testReserveOrder_ShouldThrowException_WhenOrderNotFound() {
        // given
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(999L)
                .build();
        when(salesOrderRepository.findById(999L))
                .thenReturn(Optional.empty());

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderReserveService.reserveOrder(dto));
        assertTrue(ex.getMessage().contains("Commande non trouvée"));
    }

    @Test
    void testReserveOrder_ShouldThrowException_WhenOrderNotCreated() {
        // given
        order.setStatus(Status.RESERVED);
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();
        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderReserveService.reserveOrder(dto));
        assertTrue(ex.getMessage().contains("Commande non trouvée"));
    }

    @Test
    void testReserveOrder_ShouldThrowException_WhenInventoryNotFound() {
        // given
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();
        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .thenReturn(Optional.empty());

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderReserveService.reserveOrder(dto));
        assertTrue(ex.getMessage().contains("Inventaire non trouvé"));
    }
}
