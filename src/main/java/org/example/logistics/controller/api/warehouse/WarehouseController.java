package org.example.logistics.controller.api.warehouse;

import jakarta.validation.Valid;
import org.example.logistics.dto.warehouse.WarehouseCreateDto;
import org.example.logistics.dto.warehouse.WarehouseResponseDto;
import org.example.logistics.dto.warehouse.WarehouseUpdateDto;
import org.example.logistics.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {
    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    public ResponseEntity<List<WarehouseResponseDto>> getAllWarehouses() {
        List<WarehouseResponseDto> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(warehouses);
    }


    @GetMapping("/{code}")
    public ResponseEntity<WarehouseResponseDto> getWarehouseByCode(@PathVariable String code) {
        WarehouseResponseDto warehouse = warehouseService.getWarehouseByCode(code);
        return ResponseEntity.ok(warehouse);
    }


    @PostMapping
    public ResponseEntity<WarehouseResponseDto> createWarehouse(@Valid @RequestBody WarehouseCreateDto dto) {
        WarehouseResponseDto warehouse = warehouseService.createWarehouse(dto);
        return ResponseEntity.ok(warehouse);
    }

    @PutMapping("/{code}")
    public ResponseEntity<WarehouseResponseDto> updateWarehouse(@PathVariable String code, @Valid @RequestBody WarehouseUpdateDto dto) {
        WarehouseResponseDto warehouse = warehouseService.updateWarehouse(code, dto);
        return ResponseEntity.ok(warehouse);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<String> deleteWarehouse(@PathVariable String code) {
        String message = warehouseService.deleteWarehouse(code);
        return ResponseEntity.ok(message);
    }
}
