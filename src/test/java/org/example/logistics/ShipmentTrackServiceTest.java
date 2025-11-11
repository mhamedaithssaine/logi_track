package org.example.logistics;

import org.example.logistics.dto.shipment.ShipmentTrackDto;
import org.example.logistics.dto.shipment.ShipmentTrackResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.example.logistics.mapper.shipment.ShipmentTrackMapper;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.example.logistics.service.shipment.ShipmentTrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipmentTrackServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentTrackMapper shipmentTrackMapper;

    @InjectMocks
    private ShipmentTrackService shipmentTrackService;

    private SalesOrder order;
    private Shipment shipment;
    private ShipmentTrackDto trackDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        order = SalesOrder.builder()
                .id(100L)
                .status(Status.RESERVED)
                .build();

        shipment = Shipment.builder()
                .id(1L)
                .salesOrder(order)
                .trackingNumber("TRK123")
                .status(Status_shipment.IN_TRANSIT)
                .build();

        trackDto = ShipmentTrackDto.builder()
                .orderId(100L)
                .build();
    }

    @Test
    void testTrackShipment_ShouldReturnTrackingInfo() {
        when(salesOrderRepository.findById(100L))
                .thenReturn(Optional.of(order));
        when(shipmentRepository.findBySalesOrderId(100L))
                .thenReturn(Optional.of(shipment));
        when(shipmentTrackMapper.toDto(shipment))
                .thenReturn(ShipmentTrackResponseDto.builder()
                        .id(1L)
                        .trackingNumber("TRK123")
                        .status("IN_TRANSIT")
                        .build());

        ShipmentTrackResponseDto result = shipmentTrackService.trackShipment(trackDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TRK123", result.getTrackingNumber());
        assertEquals("IN_TRANSIT", result.getStatus());
    }

    @Test
    void testTrackShipment_ShouldThrowException_WhenOrderNotFound() {
        when(salesOrderRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> shipmentTrackService.trackShipment(trackDto));
        assertTrue(ex.getMessage().contains("Commande non trouvée"));
    }

    @Test
    void testTrackShipment_ShouldThrowException_WhenShipmentNotFound() {
        when(salesOrderRepository.findById(100L))
                .thenReturn(Optional.of(order));
        when(shipmentRepository.findBySalesOrderId(100L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> shipmentTrackService.trackShipment(trackDto));
        assertTrue(ex.getMessage().contains("Aucune expédition trouvée"));
    }

    @Test
    void testUpdateStatus_ShouldUpdateSuccessfully() {
        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class)))
                .thenReturn(shipment);
        when(shipmentTrackMapper.toDto(any(Shipment.class)))
                .thenReturn(ShipmentTrackResponseDto.builder()
                        .status("DELIVERED")
                        .build());

        ShipmentTrackResponseDto result = shipmentTrackService.updateStatus(1L, Status_shipment.DELIVERED);

        assertNotNull(result);
        assertEquals("DELIVERED", result.getStatus());
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void testUpdateStatus_ShouldThrowException_WhenShipmentNotFound() {
        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> shipmentTrackService.updateStatus(1L, Status_shipment.DELIVERED));
        assertTrue(ex.getMessage().contains("Shipment non trouvé"));
    }
}