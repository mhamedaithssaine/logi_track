package org.example.logistics.controller.api.shipment;

import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.dto.shipment.ShipmentUpdateDto;
import org.example.logistics.service.shipment.ShipmentUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class ShipmentUpdateController {

    private final ShipmentUpdateService shipmentUpdateService;

    public ShipmentUpdateController(ShipmentUpdateService shipmentUpdateService) {
        this.shipmentUpdateService = shipmentUpdateService;
    }

    // déclenche l'expédition (ship) par orderId
    @PostMapping("/{id}/ship")
    public ResponseEntity<ShipmentFullResponseDto> shipOrder(@PathVariable Long id) {
        ShipmentUpdateDto dto = ShipmentUpdateDto.builder().orderId(id).build();
        ShipmentFullResponseDto resp = shipmentUpdateService.shipOrder(dto);
        return ResponseEntity.ok(resp);
    }

    // confirmer livraison par shipment id
    @PostMapping("/shipments/{id}/deliver")
    public ResponseEntity<ShipmentFullResponseDto> deliver(@PathVariable Long id) {
        ShipmentUpdateDto dto = ShipmentUpdateDto.builder().shipmentId(id).build();
        ShipmentFullResponseDto resp = shipmentUpdateService.deliverShipment(dto);
        return ResponseEntity.ok(resp);
    }
}
