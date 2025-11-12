package org.example.logistics.service.shipment;

import lombok.RequiredArgsConstructor;
import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.dto.shipment.ShipmentTrackDto;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.shipment.ShipmentTrackMapper;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentTrackService {

    private final SalesOrderRepository salesOrderRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackMapper shipmentTrackMapper;

    public ShipmentFullResponseDto trackShipment(ShipmentTrackDto dto) {
        SalesOrder order = salesOrderRepository.findById(dto.getOrderId())
                .orElseThrow(() ->  ResourceNotFoundException.withId("Commande non trouvée", dto.getOrderId()));

        Shipment shipment = shipmentRepository.findBySalesOrderId(dto.getOrderId())
                .orElseThrow(() ->  ResourceNotFoundException.withId("Aucune expédition trouvée pour cette commande", dto.getOrderId()));

        ShipmentFullResponseDto dtoOut = shipmentTrackMapper.toDto(shipment);
        dtoOut.setLines(order.getLines() == null ? Collections.emptyList() :
                order.getLines().stream()
                        .map(l -> ShipmentFullResponseDto.LineInfo.builder()
                                .sku(l.getProduct().getSku())
                                .quantity(l.getQuantity())
                                .build())
                        .toList()
        );
        return dtoOut;
    }

    public ShipmentFullResponseDto updateStatus(Long shipmentId, Status_shipment newStatus) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->  ResourceNotFoundException.withId("Shipment non trouvé",shipmentId));

        shipment.setStatus(newStatus);
        if (newStatus == Status_shipment.IN_TRANSIT) {
            shipment.setShippedAt(java.time.LocalDateTime.now());
        } else if (newStatus == Status_shipment.DELIVERED) {
            shipment.setDeliveredAt(java.time.LocalDateTime.now());
        }

        Shipment saved = shipmentRepository.save(shipment);
        return shipmentTrackMapper.toDto(saved);
    }

    // Get By id
    public ShipmentFullResponseDto getById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() ->  ResourceNotFoundException.withId("Expédition non trouvée avec ID: " , id));
        return shipmentTrackMapper.toDto(shipment);
    }

    // List shipment
    public List<ShipmentFullResponseDto> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(shipmentTrackMapper::toDto)
                .toList();
    }
}
