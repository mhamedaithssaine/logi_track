package org.example.logistics.service.shipment;

import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.dto.shipment.ShipmentTrackDto;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.shipment.ShipmentTrackMapper;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentTrackServiceTest {

    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private ShipmentTrackMapper shipmentTrackMapper;

    @InjectMocks private ShipmentTrackService shipmentTrackService;

    private SalesOrder order;
    private Shipment shipment;

    @BeforeEach
    void setup() {
        order = new SalesOrder();
        order.setId(1L);
        order.setLines(Collections.emptyList());

        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setStatus(Status_shipment.PLANNED);
        shipment.setSalesOrder(order);
    }

    @Test
    void shouldTrackShipment_whenOrderExists() {
        when(salesOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(shipmentRepository.findBySalesOrderId(order.getId())).thenReturn(Optional.of(shipment));

        when(shipmentTrackMapper.toDto(shipment)).thenReturn(
                ShipmentFullResponseDto.builder()
                        .shipmentId(shipment.getId())
                        .orderId(order.getId())
                        .status("PLANNED")
                        .lines(Collections.emptyList())
                        .build()
        );

        var dtoOut = shipmentTrackService.trackShipment(
                ShipmentTrackDto.builder().orderId(order.getId()).build()
        );
        assertNotNull(dtoOut);
    }


    @Test
    void shouldThrow_whenShipmentNotFound() {
        when(salesOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(shipmentRepository.findBySalesOrderId(order.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shipmentTrackService.trackShipment(ShipmentTrackDto.builder().orderId(order.getId()).build()));
    }
}
