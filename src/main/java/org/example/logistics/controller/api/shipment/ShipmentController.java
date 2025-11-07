package org.example.logistics.controller.api.shipment;

import jakarta.validation.Valid;
import org.example.logistics.dto.shipment.ShipmentCreateDto;
import org.example.logistics.dto.shipment.ShipmentResponseDto;
import org.example.logistics.service.shipment.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/api/shipments")
public class ShipmentController {
    @Autowired
    private ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponseDto> createShipment(@Valid @RequestBody ShipmentCreateDto dto) {
        ShipmentResponseDto response = shipmentService.createShipment(dto);
        return ResponseEntity.ok(response);
    }
}
