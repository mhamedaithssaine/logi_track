package org.example.logistics.service.shipment;

import lombok.RequiredArgsConstructor;
import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.dto.shipment.ShipmentUpdateDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.MovementType;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.exception.BadRequestException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.repository.InventoryMovementRepository;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ShipmentUpdateService {

    private final SalesOrderRepository salesOrderRepository;
    private final ShipmentRepository shipmentRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryRepository inventoryRepository;

    public ShipmentFullResponseDto shipOrder(ShipmentUpdateDto dto) {
        SalesOrder order = salesOrderRepository.findByIdAndStatus(dto.getOrderId(), Status.RESERVED)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée ou non réservée"));

        Shipment shipment = shipmentRepository.findBySalesOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucune expédition trouvée"));

        shipment.setStatus(Status_shipment.IN_TRANSIT);
        shipment.setShippedAt(LocalDateTime.now());
        shipmentRepository.save(shipment);

        if (order.getLines() != null) {
            for (SalesOrderLine line : order.getLines()) {
                InventoryMovement mv = InventoryMovement.builder()
                        .product(line.getProduct())
                        .warehouse(order.getWarehouse())
                        .type(MovementType.OUTBOUND)
                        .quantity(line.getQuantity())
                        .referenceDoc("SO" + order.getId())
                        .occurredAt(LocalDateTime.now())
                        .build();
                inventoryMovementRepository.save(mv);

                inventoryRepository.findByProductIdAndWarehouseId(
                                line.getProduct().getId(), order.getWarehouse().getId())
                        .ifPresent(inv -> {
                            inv.setQtyReserved(Math.max(0, inv.getQtyReserved() - line.getQuantity()));
                            inventoryRepository.save(inv);
                        });

                line.setBackorderQty(0);
            }
        }

        order.setStatus(Status.SHIPPED);
        salesOrderRepository.save(order);

        return ShipmentFullResponseDto.builder()
                .shipmentId(shipment.getId())
                .orderId(order.getId())
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .status("SHIPPED")
                .message("Commande expédiée")
                .plannedDeparture(shipment.getPlannedDeparture())
                .shippedAt(shipment.getShippedAt())
                .lines(order.getLines() == null ? Collections.emptyList() :
                        order.getLines().stream()
                                .map(l -> ShipmentFullResponseDto.LineInfo.builder()
                                        .sku(l.getProduct().getSku())
                                        .quantity(l.getQuantity())
                                        .build())
                                .toList())
                .build();
    }

    public ShipmentFullResponseDto deliverShipment(ShipmentUpdateDto dto) {
        Shipment shipment = shipmentRepository.findById(dto.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment non trouvé"));

        if (shipment.getStatus() != Status_shipment.IN_TRANSIT) {
            throw new BadRequestException("Shipment non en transit");
        }

        shipment.setStatus(Status_shipment.DELIVERED);
        shipment.setDeliveredAt(LocalDateTime.now());
        shipmentRepository.save(shipment);

        SalesOrder order = shipment.getSalesOrder();
        order.setStatus(Status.DELIVERED);
        salesOrderRepository.save(order);

        return ShipmentFullResponseDto.builder()
                .shipmentId(shipment.getId())
                .orderId(order.getId())
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .status("DELIVERED")
                .message("Livraison confirmée")
                .deliveredAt(shipment.getDeliveredAt())
                .lines(order.getLines() == null ? Collections.emptyList() :
                        order.getLines().stream()
                                .map(l -> ShipmentFullResponseDto.LineInfo.builder()
                                        .sku(l.getProduct().getSku())
                                        .quantity(l.getQuantity())
                                        .build())
                                .toList())
                .build();
    }
}
