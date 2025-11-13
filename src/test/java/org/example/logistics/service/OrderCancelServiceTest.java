package org.example.logistics.service;

import org.example.logistics.dto.order.OrderCancelDto;
import org.example.logistics.dto.order.OrderCancelResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.OrderCancelMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderCancelServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderCancelMapper orderCancelMapper;

    @InjectMocks
    private OrderCancelService orderCancelService;

    private SalesOrder order;
    private SalesOrderLine line;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 🧱 Création des objets de test
        Warehouse warehouse = Warehouse.builder()
                .id(1L)
                .code("W1")
                .name("Main Warehouse")
                .build();

        Product product = Product.builder()
                .id(10L)
                .sku("SKU001")
                .name("Produit Test")
                .category("CAT-A")
                .price(100.0)
                .active(true)
                .build();

        line = SalesOrderLine.builder()
                .id(100L)
                .product(product)
                .quantity(5)
                .backorderQty(0)
                .build();

        order = SalesOrder.builder()
                .id(1L)
                .status(Status.RESERVED)
                .warehouse(warehouse)
                .lines(List.of(line))
                .build();

        // ✅ correspond à ton entity réelle
        inventory = Inventory.builder()
                .id(50L)
                .product(product)
                .warehouse(warehouse)
                .qtyOnHand(30)
                .qtyReserved(10)
                .build();
    }

    @Test
    void shouldCancelOrderSuccessfully() {
        OrderCancelDto dto = OrderCancelDto.builder()
                .orderId(1L)
                .cancelReason("Client changed mind")
                .build();

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(10L, 1L))
                .thenReturn(Optional.of(inventory));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderCancelMapper.toDto(any(SalesOrder.class)))
                .thenAnswer(invocation -> {
                    SalesOrder saved = invocation.getArgument(0);
                    return OrderCancelResponseDto.builder()
                            .orderId(saved.getId())
                            .currentStatus(saved.getStatus().name())
                            .build();
                });

        OrderCancelResponseDto response = orderCancelService.cancelOrder(dto);

        assertNotNull(response);
        assertEquals(Status.CANCELED.name(), response.getCurrentStatus());
        assertEquals(1L, response.getOrderId());
        assertEquals("RESERVED", response.getPreviousStatus());
        assertEquals(5, response.getStockFreed());
        assertEquals(1, response.getLinesCanceled());
        assertTrue(response.getMessage().contains("annulée avec succès"));

        verify(inventoryRepository).save(any(Inventory.class));
        verify(salesOrderRepository).save(any(SalesOrder.class));
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        OrderCancelDto dto = OrderCancelDto.builder().orderId(99L).build();
        when(salesOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderCancelService.cancelOrder(dto));
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenStatusNotCancelable() {
        order.setStatus(Status.SHIPPED);
        OrderCancelDto dto = OrderCancelDto.builder().orderId(1L).build();
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> orderCancelService.cancelOrder(dto));

        assertTrue(ex.getMessage().contains("Impossible d'annuler"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenInventoryNotFound() {
        OrderCancelDto dto = OrderCancelDto.builder().orderId(1L).build();
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderCancelService.cancelOrder(dto));
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void shouldHandleOrderWithoutLinesGracefully() {
        order.setLines(null);
        OrderCancelDto dto = OrderCancelDto.builder().orderId(1L).build();

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderCancelMapper.toDto(any(SalesOrder.class)))
                .thenReturn(OrderCancelResponseDto.builder()
                        .orderId(1L)
                        .currentStatus(Status.CANCELED.name())
                        .build());

        OrderCancelResponseDto response = orderCancelService.cancelOrder(dto);

        assertNotNull(response);
        assertEquals(Status.CANCELED.name(), response.getCurrentStatus());
        assertNotNull(order.getCanceledAt());
    }
}
