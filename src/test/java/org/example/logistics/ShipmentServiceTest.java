package org.example.logistics.service;

import org.example.logistics.dto.shipment.ShipmentCreateDto;
import org.example.logistics.dto.shipment.ShipmentResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.example.logistics.entity.Client;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.mapper.shipment.ShipmentMapper;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.example.logistics.service.shipment.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipmentServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @InjectMocks
    private ShipmentService shipmentService;

    private SalesOrder order;
    private Shipment shipment;
    private ShipmentCreateDto createDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(shipmentService, "cutoffHour", 15);

        Client client = Client.builder().id(1L).build();
        Warehouse warehouse = Warehouse.builder().id(1L).build();

        order = SalesOrder.builder()
                .id(100L)
                .client(client)
                .warehouse(warehouse)
                .status(Status.RESERVED)
                .build();

        shipment = Shipment.builder()
                .id(1L)
                .salesOrder(order)
                .carrier("DHL")
                .trackingNumber("TRK123")
                .status(Status_shipment.PLANNED)
                .build();

        createDto = ShipmentCreateDto.builder()
                .orderId(100L)
                .carrier("DHL")
                .trackingNumber("TRK123")
                .build();
    }

    @Test
    void testCreateShipment_ShouldCreateSuccessfully() {
        when(salesOrderRepository.findByIdAndStatus(100L, Status.RESERVED))
                .thenReturn(Optional.of(order));
        when(shipmentMapper.toEntity(createDto)).thenReturn(shipment);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(shipmentMapper.toDto(shipment)).thenReturn(ShipmentResponseDto.builder()
                .id(1L)
                .carrier("DHL")
                .trackingNumber("TRK123")
                .status("PLANNED")
                .build());

        ShipmentResponseDto result = shipmentService.createShipment(createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DHL", result.getCarrier());
        assertEquals(100L, result.getOrderId());
        assertTrue(result.getMessage().contains("Expédition créée"));
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void testCreateShipment_ShouldThrowException_WhenOrderNotFound() {
        when(salesOrderRepository.findByIdAndStatus(100L, Status.RESERVED))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> shipmentService.createShipment(createDto));
        assertTrue(ex.getMessage().contains("Commande non trouvée"));
        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void testCreateShipment_ShouldRespectCutOffTime() {
        when(salesOrderRepository.findByIdAndStatus(100L, Status.RESERVED))
                .thenReturn(Optional.of(order));
        when(shipmentMapper.toEntity(createDto)).thenReturn(shipment);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(shipmentMapper.toDto(any(Shipment.class))).thenReturn(ShipmentResponseDto.builder().build());

        shipmentService.createShipment(createDto);

        verify(shipmentRepository).save(argThat(s -> s.getPlannedDeparture() != null));
    }

    @Test
    void testShipmentStatusTransition_ShouldWorkCorrectly() {
        shipment.setStatus(Status_shipment.PLANNED);
        shipment.setStatus(Status_shipment.IN_TRANSIT);
        assertEquals(Status_shipment.IN_TRANSIT, shipment.getStatus());
        assertFalse(shipment.isDelivered());
    }

    @Test
    void testShipmentDelivered_ShouldReturnTrue() {
        shipment.setStatus(Status_shipment.DELIVERED);
        shipment.setDeliveredAT(LocalDateTime.now());
        assertTrue(shipment.isDelivered());
        assertNotNull(shipment.getDeliveredAT());
    }
}
