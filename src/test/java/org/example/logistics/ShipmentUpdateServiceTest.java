package org.example.logistics;

import org.example.logistics.dto.shipment.ShipmentUpdateDto;
import org.example.logistics.dto.shipment.ShipmentUpdateResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.mapper.shipment.ShipmentUpdateMapper;
import org.example.logistics.repository.*;
import org.example.logistics.service.shipment.ShipmentUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipmentUpdateServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ShipmentUpdateMapper shipmentUpdateMapper;

    @InjectMocks
    private ShipmentUpdateService shipmentUpdateService;

    private SalesOrder order;
    private Shipment shipment;
    private SalesOrderLine orderLine;
    private Inventory inventory;
    private ShipmentUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Product product = Product.builder().id(1L).sku("SKU-TEST").build();
        Warehouse warehouse = Warehouse.builder().id(1L).build();

        orderLine = SalesOrderLine.builder()
                .id(1L)
                .product(product)
                .quantity(10)
                .backorderQty(0)
                .build();

        order = SalesOrder.builder()
                .id(100L)
                .status(Status.RESERVED)
                .warehouse(warehouse)
                .lines(Collections.singletonList(orderLine))
                .build();

        shipment = Shipment.builder()
                .id(1L)
                .salesOrder(order)
                .trackingNumber("TRK123")
                .status(Status_shipment.IN_TRANSIT)
                .build();

        inventory = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .qtyReserved(10)
                .build();

        updateDto = ShipmentUpdateDto.builder()
                .orderId(100L)
                .shipmentId(1L)
                .build();
    }

    @Test
    void testShipOrder_ShouldShipSuccessfully() {
        when(salesOrderRepository.findByIdAndStatus(100L, Status.RESERVED))
                .thenReturn(Optional.of(order));
        when(shipmentRepository.findBySalesOrderId(100L))
                .thenReturn(Optional.of(shipment));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        ShipmentUpdateResponseDto result = shipmentUpdateService.shipOrder(updateDto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("SHIPPED", result.getStatus());
        assertEquals("Commande expédiée", result.getMessage());
        assertEquals("TRK123", result.getTrackingNumber());

        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
        verify(inventoryRepository).save(any(Inventory.class));
        verify(salesOrderRepository).save(any(SalesOrder.class));
        assertEquals(Status.SHIPPED, order.getStatus());
        assertEquals(0, inventory.getQtyReserved());
    }

    @Test
    void testShipOrder_ShouldThrowException_WhenOrderNotFound() {
        when(salesOrderRepository.findByIdAndStatus(100L, Status.RESERVED))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> shipmentUpdateService.shipOrder(updateDto));
        assertTrue(ex.getMessage().contains("Commande non trouvée"));
    }

    @Test
    void testDeliverShipment_ShouldDeliverSuccessfully() {
        shipment.setStatus(Status_shipment.IN_TRANSIT);
        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        ShipmentUpdateResponseDto result = shipmentUpdateService.deliverShipment(updateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DELIVERED", result.getStatus());
        assertEquals("Livraison confirmée", result.getMessage());
        assertEquals("TRK123", result.getTrackingNumber());

        verify(shipmentRepository).save(any(Shipment.class));
        verify(salesOrderRepository).save(any(SalesOrder.class));
        assertEquals(Status_shipment.DELIVERED, shipment.getStatus());
        assertEquals(Status.DELIVERED, order.getStatus());
        assertNotNull(shipment.getDeliveredAT());
    }

    @Test
    void testDeliverShipment_ShouldThrowException_WhenShipmentNotInTransit() {
        shipment.setStatus(Status_shipment.PLANNED);
        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> shipmentUpdateService.deliverShipment(updateDto));
        assertTrue(ex.getMessage().contains("Shipment non trouvé ou non en transit"));
    }

    @Test
    void testShipOrder_ShouldResetBackorderQuantity() {
        orderLine.setBackorderQty(5);
        when(salesOrderRepository.findByIdAndStatus(100L, Status.RESERVED))
                .thenReturn(Optional.of(order));
        when(shipmentRepository.findBySalesOrderId(100L))
                .thenReturn(Optional.of(shipment));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        shipmentUpdateService.shipOrder(updateDto);

        assertEquals(0, orderLine.getBackorderQty());
    }
}