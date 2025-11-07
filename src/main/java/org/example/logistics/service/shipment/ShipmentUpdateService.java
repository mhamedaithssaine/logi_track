package org.example.logistics.service.shipment;

import org.example.logistics.dto.shipment.ShipmentUpdateDto;
import org.example.logistics.dto.shipment.ShipmentUpdateResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.MovementType;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.mapper.shipment.ShipmentUpdateMapper;
import org.example.logistics.repository.InventoryMovementRepository;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ShipmentUpdateService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private ShipmentUpdateMapper shipmentUpdateMapper;

    @Autowired
    private InventoryRepository inventoryRepository;


    public ShipmentUpdateResponseDto shipOrder(ShipmentUpdateDto dto) {
        Optional<SalesOrder> optOrder = salesOrderRepository.findByIdAndStatus(dto.getOrderId(), Status.RESERVED);
        if (optOrder.isEmpty()) {
            throw new RuntimeException("Commande non trouvée ou non réservée");
        }

        SalesOrder order = optOrder.get();

        Optional<Shipment> optShipment = shipmentRepository.findBySalesOrderId(dto.getOrderId());
        String trackingNumber = optShipment.map(Shipment::getTrackingNumber).orElse("TRACK" + order.getId());

        // Create OUTBOUND
        for (SalesOrderLine line : order.getLines()) {
            InventoryMovement movement = InventoryMovement.builder()
                    .product(line.getProduct())
                    .warehouse(order.getWarehouse())
                    .type(MovementType.OUTBOUND)
                    .quantity(-line.getQuantity())
                    .referenceDoc("SO" + order.getId())
                    .occurredAt(LocalDateTime.now())
                    .build();
            inventoryMovementRepository.save(movement);

            Optional<Inventory> optInv = inventoryRepository.findByProductIdAndWarehouseId(
                    line.getProduct().getId(), order.getWarehouse().getId());
            if (optInv.isPresent()) {
                Inventory inv = optInv.get();
                inv.setQtyReserved(0);
                inventoryRepository.save(inv);
            }

            // Reset backorder si partiel
            line.setBackorderQty(0);
        }

        order.setStatus(Status.SHIPPED);
        salesOrderRepository.save(order);

        return ShipmentUpdateResponseDto.builder()
                .id(order.getId())
                .status("SHIPPED")
                .message("Commande expédiée")
                .trackingNumber(trackingNumber)
                .build();
    }



    public ShipmentUpdateResponseDto deliverShipment(ShipmentUpdateDto dto) {
        Optional<Shipment> optShipment = shipmentRepository.findById(dto.getShipmentId());
        if (optShipment.isEmpty() || optShipment.get().getStatus() != Status_shipment.IN_TRANSIT) {
            throw new RuntimeException("Shipment non trouvé ou non en transit");
        }

        Shipment shipment = optShipment.get();
        shipment.setStatus(Status_shipment.DELIVERED);
        shipment.setDeliveredAT(LocalDateTime.now());
        shipmentRepository.save(shipment);


        SalesOrder order = shipment.getSalesOrder();
        order.setStatus(Status.DELIVERED);
        salesOrderRepository.save(order);

        return ShipmentUpdateResponseDto.builder()
                .id(shipment.getId())
                .status("DELIVERED")
                .message("Livraison confirmée")
                .trackingNumber(shipment.getTrackingNumber())
                .build();
    }
}
