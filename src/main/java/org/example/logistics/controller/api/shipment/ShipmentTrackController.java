package org.example.logistics.controller.api.shipment;

import org.example.logistics.dto.shipment.ShipmentTrackDto;
import org.example.logistics.dto.shipment.ShipmentTrackResponseDto;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.service.shipment.ShipmentTrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")

public class ShipmentTrackController {


    @Autowired
    private ShipmentTrackService shipmentTrackService;


    @GetMapping("/{id}/shipment")
    public ResponseEntity<ShipmentTrackResponseDto> trackShipment(@PathVariable Long id) {
        ShipmentTrackDto dto = new ShipmentTrackDto();
        dto.setOrderId(id);
        ShipmentTrackResponseDto response = shipmentTrackService.trackShipment(dto);
        return ResponseEntity.ok(response);
    }

    // Update status
    @PutMapping("/{id}/shipment/status")
    public ResponseEntity<ShipmentTrackResponseDto> updateStatus(@PathVariable Long id, @RequestParam Status_shipment newStatus) {
        ShipmentTrackResponseDto response = shipmentTrackService.updateStatus(id, newStatus);
        return ResponseEntity.ok(response);
    }
}
