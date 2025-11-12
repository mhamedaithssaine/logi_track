package org.example.logistics.controller.api.shipment;

import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.dto.shipment.ShipmentTrackDto;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.service.shipment.ShipmentTrackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentTrackController {

    private final ShipmentTrackService shipmentTrackService;

    public ShipmentTrackController(ShipmentTrackService shipmentTrackService) {
        this.shipmentTrackService = shipmentTrackService;
    }

    @PostMapping("/track")
    public ResponseEntity<ShipmentFullResponseDto> track(@Valid @RequestBody ShipmentTrackDto dto) {
        ShipmentFullResponseDto response = shipmentTrackService.trackShipment(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ShipmentFullResponseDto> updateStatus(@PathVariable Long id, @RequestParam Status_shipment status) {
        ShipmentFullResponseDto resp = shipmentTrackService.updateStatus(id, status);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentFullResponseDto> getById(@PathVariable Long id) {
        ShipmentFullResponseDto response = shipmentTrackService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ShipmentFullResponseDto>> getAll() {
        return ResponseEntity.ok(shipmentTrackService.getAllShipments());
    }
}
