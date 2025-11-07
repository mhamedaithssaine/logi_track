package org.example.logistics.controller.api.shipment;

import org.example.logistics.dto.shipment.ShipmentUpdateDto;
import org.example.logistics.dto.shipment.ShipmentUpdateResponseDto;
import org.example.logistics.service.shipment.ShipmentUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class ShipmentUpdateController {

    @Autowired
    private ShipmentUpdateService shipmentUpdateService;

    @PostMapping("/{id}/ship")
    public ResponseEntity<ShipmentUpdateResponseDto> shiOrder(@PathVariable Long id){
        ShipmentUpdateDto dto = ShipmentUpdateDto.builder()
                .orderId(id)
                .build();
        ShipmentUpdateResponseDto responseDto = shipmentUpdateService.shipOrder(dto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/shipments/{id}/deliver")
    public ResponseEntity<ShipmentUpdateResponseDto> deliverShipment(@PathVariable Long id) {
        ShipmentUpdateDto dto = ShipmentUpdateDto.builder()
                .shipmentId(id)
                .build();
        ShipmentUpdateResponseDto response = shipmentUpdateService.deliverShipment(dto);
        return ResponseEntity.ok(response);
    }
}
