package org.example.logistics.service.shipment;

import lombok.RequiredArgsConstructor;
import org.example.logistics.dto.shipment.ShipmentCreateDto;
import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.shipment.ShipmentMapper;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor // ✅ Génère automatiquement un constructeur pour les final fields
public class ShipmentService {

    private final SalesOrderRepository salesOrderRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    @Value("${logistics.cutoff-hour:15}")
    private int cutoffHour;

    public ShipmentFullResponseDto createShipment(ShipmentCreateDto dto) {
        SalesOrder order = salesOrderRepository.findByIdAndStatus(dto.getOrderId(), Status.RESERVED)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée ou non réservée"));

        Shipment shipment = shipmentMapper.toEntity(dto);
        shipment.setSalesOrder(order);
        shipment.setPlannedDeparture(calculatePlannedDeparture());

        Shipment saved = shipmentRepository.save(shipment);
        return shipmentMapper.toFullDto(saved);
    }

    private LocalDateTime calculatePlannedDeparture() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.withHour(cutoffHour).withMinute(0).withSecond(0);
        return now.isAfter(cutoff) ? cutoff.plusDays(1) : cutoff;
    }
}
