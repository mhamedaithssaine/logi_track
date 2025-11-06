package org.example.logistics.service.shipment;

import org.example.logistics.dto.shipment.ShipmentTrackDto;
import org.example.logistics.dto.shipment.ShipmentTrackResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.example.logistics.mapper.shipment.ShipmentTrackMapper;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ShipmentTrackService {
    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ShipmentTrackMapper shipmentTrackMapper;


    public ShipmentTrackResponseDto trackShipment(ShipmentTrackDto dto) {
        Optional<SalesOrder> optOrder = salesOrderRepository.findById(dto.getOrderId());
        if (optOrder.isEmpty() || optOrder.get().getStatus() != Status.RESERVED) {
            throw new RuntimeException("Commande non trouvée ou non réservée (pas d'expédition)");
        }

        Optional<Shipment> optShipment = shipmentRepository.findBySalesOrderId(dto.getOrderId());
        if (optShipment.isEmpty()) {
            throw new RuntimeException("Aucune expédition trouvée pour cette commande");
        }

        Shipment shipment = optShipment.get();
        return shipmentTrackMapper.toDto(shipment);
    }

    public ShipmentTrackResponseDto updateStatus(Long shipmentId, Status_shipment newStatus) {
        Optional<Shipment> optShipment = shipmentRepository.findById(shipmentId);
        if (optShipment.isEmpty()) {
            throw new RuntimeException("Shipment non trouvé");
        }

        Shipment shipment = optShipment.get();
        shipment.setStatus(newStatus);
        shipmentRepository.save(shipment);

        return shipmentTrackMapper.toDto(shipment);
    }
}