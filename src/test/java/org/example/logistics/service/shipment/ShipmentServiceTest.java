package org.example.logistics.service.shipment;

import org.example.logistics.dto.shipment.ShipmentCreateDto;
import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.shipment.ShipmentMapper;
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
class ShipmentServiceTest {

    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private ShipmentMapper shipmentMapper;

    @InjectMocks private ShipmentService shipmentService;

    private SalesOrder order;
    private Shipment shipment;

    @BeforeEach
    void setup() {
        order = new SalesOrder();
        order.setId(1L);
        order.setStatus(Status.RESERVED);

        shipment = new Shipment();
        shipment.setId(1L);
    }

    @Test
    void shouldCreateShipment_whenOrderReserved() {
        ShipmentCreateDto dto = ShipmentCreateDto.builder().orderId(order.getId()).build();

        when(salesOrderRepository.findByIdAndStatus(order.getId(), Status.RESERVED))
                .thenReturn(Optional.of(order));
        when(shipmentMapper.toEntity(dto)).thenReturn(shipment);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        // ✅ Utiliser builder au lieu de `new ShipmentFullResponseDto()`
        when(shipmentMapper.toFullDto(shipment)).thenReturn(
                ShipmentFullResponseDto.builder()
                        .shipmentId(shipment.getId())
                        .orderId(order.getId())
                        .status("PLANNED")
                        .lines(Collections.emptyList())
                        .build()
        );

        ShipmentFullResponseDto response = shipmentService.createShipment(dto);
        assertNotNull(response);
        verify(shipmentRepository).save(shipment);
    }


    @Test
    void shouldThrow_whenOrderNotReserved() {
        ShipmentCreateDto dto = ShipmentCreateDto.builder().orderId(order.getId()).build();
        when(salesOrderRepository.findByIdAndStatus(order.getId(), Status.RESERVED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shipmentService.createShipment(dto));
    }
}
