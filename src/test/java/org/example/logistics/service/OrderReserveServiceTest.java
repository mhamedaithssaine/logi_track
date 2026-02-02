package org.example.logistics.service;

import org.example.logistics.dto.order.SalesOrderReserveDto;
import org.example.logistics.dto.order.SalesOrderReserveResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.Product;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.OrderReserveMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
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

        line = SalesOrderLine.builder()
                .product(product)
                .quantity(10)
                .build();

        order = SalesOrder.builder()
                .id(100L)
                .status(Status.CREATED)
                .warehouse(
                        org.example.logistics.entity.Warehouse.builder().id(1L).name("WH-Paris").build()
                )
                .lines(Collections.singletonList(line))
                .build();

        inventory = Inventory.builder()
                .id(10L)
                .product(product)
                .warehouse(order.getWarehouse())
                .qtyOnHand(0)
                .qtyReserved(0)
                .build();
    }

    // Cas 1 : commande non trouvée -> ResourceNotFoundException
    @Test
    void shouldThrow_whenOrderNotFound() {
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(999L)
                .build();

        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> orderReserveService.reserveOrder(dto));

        assertTrue(ex.getMessage().contains("Commande"));
        verifyNoInteractions(inventoryRepository);
    }

    // Cas 2 : commande non créée (status != CREATED) -> IllegalStateException (adapté au service)
    @Test
    void shouldThrow_whenOrderNotCreated() {
        order.setStatus(Status.RESERVED);

        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> orderReserveService.reserveOrder(dto));


        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        boolean ok = msg.contains(String.valueOf(order.getId())) ||
                msg.contains("cré") ||
                msg.contains("non créée") ||
                msg.contains("pas créée") ||
                msg.contains("non cree"); // fallback sans accent
        assertTrue(ok, "Le message doit indiquer que la commande n'est pas créée ou contenir l'id de la commande. message='" + ex.getMessage() + "'");
        verifyNoInteractions(inventoryRepository);
    }

    // Cas 3 : inventaire non trouvé -> ConflictException
    @Test
    void shouldThrow_whenInventoryNotFound() {
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), order.getWarehouse().getId()))
                .thenReturn(Optional.empty());

        ConflictException ex = assertThrows(ConflictException.class,
                () -> orderReserveService.reserveOrder(dto));

        assertTrue(ex.getMessage().toLowerCase().contains("inventaire") || ex.getMessage().toLowerCase().contains("inbound"),
                "Message doit indiquer inventaire absent ou INBOUND: " + ex.getMessage());
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(product.getId(), order.getWarehouse().getId());
    }

    // Cas 4 : aucun stock disponible -> backorder complet, qtyReserved inchangée
    @Test
    void shouldCreateBackorder_whenStockZero() {
        inventory.setQtyOnHand(0); // available == 0

        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), order.getWarehouse().getId()))
                .thenReturn(Optional.of(inventory));
        when(orderReserveMapper.toDto(any(SalesOrder.class)))
                .thenReturn(SalesOrderReserveResponseDto.builder().build());

        SalesOrderReserveResponseDto response = orderReserveService.reserveOrder(dto);

        assertEquals(Status.PARTIAL_RESERVED, order.getStatus());
        assertEquals(0, inventory.getQtyReserved());
        assertEquals(10, line.getBackorderQty());
        verify(inventoryRepository, times(1)).save(inventory);
        verify(salesOrderRepository, times(1)).save(order);
        assertNotNull(response);
    }

    // Cas 5 : stock insuffisant (réservation partielle)
    @Test
    void shouldReservePartial_whenStockNotEnough() {
        inventory.setQtyOnHand(5);

        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), order.getWarehouse().getId()))
                .thenReturn(Optional.of(inventory));
        when(orderReserveMapper.toDto(any(SalesOrder.class)))
                .thenReturn(SalesOrderReserveResponseDto.builder().build());

        orderReserveService.reserveOrder(dto);

        assertEquals(Status.PARTIAL_RESERVED, order.getStatus());
        assertEquals(5, inventory.getQtyReserved());
        assertEquals(5, line.getBackorderQty());
        verify(inventoryRepository, times(1)).save(inventory);
        verify(salesOrderRepository, times(1)).save(order);
    }

    // Cas 6 : stock suffisant (réservation complète)
    @Test
    void shouldReserveCompletely_whenStockSufficient() {
        inventory.setQtyOnHand(20);

        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(order.getId())
                .build();

        when(salesOrderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), order.getWarehouse().getId()))
                .thenReturn(Optional.of(inventory));
        when(orderReserveMapper.toDto(any(SalesOrder.class)))
                .thenReturn(SalesOrderReserveResponseDto.builder().build());

        orderReserveService.reserveOrder(dto);

        assertEquals(Status.RESERVED, order.getStatus());
        assertEquals(10, inventory.getQtyReserved());
        assertEquals(0, line.getBackorderQty());
        verify(inventoryRepository, times(1)).save(inventory);
        verify(salesOrderRepository, times(1)).save(order);
    }
}