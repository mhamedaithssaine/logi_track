package org.example.logistics.service.shipment;

import org.example.logistics.dto.shipment.ShipmentUpdateDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.exception.BadRequestException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.repository.InventoryMovementRepository;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentUpdateServiceTest {

    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private InventoryMovementRepository inventoryMovementRepository;
    @Mock private InventoryRepository inventoryRepository;

    @InjectMocks private ShipmentUpdateService shipmentUpdateService;

    private SalesOrder order;
    private Shipment shipment;

    @BeforeEach
    void setup() {
        order = new SalesOrder();
        order.setId(1L);
        order.setStatus(Status.RESERVED);
        order.setLines(Collections.emptyList());
        order.setWarehouse(new Warehouse(1L, "WarPari-25","WH1"));

        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setStatus(Status_shipment.PLANNED);
        shipment.setSalesOrder(order);
    }

    @Test
    void shouldShipOrder_whenOrderReserved() {
        when(salesOrderRepository.findByIdAndStatus(order.getId(), Status.RESERVED))
                .thenReturn(Optional.of(order));
        when(shipmentRepository.findBySalesOrderId(order.getId()))
                .thenReturn(Optional.of(shipment));

        var response = shipmentUpdateService.shipOrder(ShipmentUpdateDto.builder().orderId(order.getId()).build());

        assertEquals("SHIPPED", response.getStatus());
        verify(shipmentRepository).save(shipment);
        verify(salesOrderRepository).save(order);
    }

    @Test
    void shouldThrow_whenOrderNotReserved() {
        when(salesOrderRepository.findByIdAndStatus(order.getId(), Status.RESERVED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shipmentUpdateService.shipOrder(ShipmentUpdateDto.builder().orderId(order.getId()).build()));
    }

    @Test
    void shouldDeliverShipment_whenInTransit() {
        shipment.setStatus(Status_shipment.IN_TRANSIT);
        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));

        var response = shipmentUpdateService.deliverShipment(ShipmentUpdateDto.builder().shipmentId(shipment.getId()).build());

        assertEquals("DELIVERED", response.getStatus());
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void shouldThrow_whenShipmentNotInTransit() {
        shipment.setStatus(Status_shipment.PLANNED);
        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));

        assertThrows(BadRequestException.class,
                () -> shipmentUpdateService.deliverShipment(ShipmentUpdateDto.builder().shipmentId(shipment.getId()).build()));
    }
}
