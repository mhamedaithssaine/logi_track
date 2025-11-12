package org.example.logistics.controller.api.shipment;

import org.example.logistics.dto.shipment.ShipmentCreateDto;
import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.service.shipment.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/{id}/shipments")
    public ResponseEntity<ShipmentFullResponseDto> createShipment(@PathVariable Long id,
                                                                  @Valid @RequestBody ShipmentCreateDto dto) {
        dto.setOrderId(id);
        ShipmentFullResponseDto response = shipmentService.createShipment(dto);
        return ResponseEntity.ok(response);
    }


}
