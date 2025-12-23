package org.example.logistics.controller.api.warehousemanager;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.logistics.dto.warehousemanager.WarehouseManagerLoginDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerRegisterDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerResponseDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerUpdateDto;
import org.example.logistics.service.WarehouseManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse-managers")
public class WarehouseManagerController {

    @Autowired
    private  WarehouseManagerService warehouseManagerService;

    @PostMapping("/register")
    public ResponseEntity<WarehouseManagerResponseDto> register(@Valid @RequestBody WarehouseManagerRegisterDto dto) {
        WarehouseManagerResponseDto response = warehouseManagerService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<WarehouseManagerResponseDto> login(@Valid @RequestBody WarehouseManagerLoginDto dto) {
        WarehouseManagerResponseDto response = warehouseManagerService.login(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseManagerResponseDto> getById(@PathVariable Long id) {
        WarehouseManagerResponseDto response = warehouseManagerService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<WarehouseManagerResponseDto> getByEmail(@PathVariable String email) {
        WarehouseManagerResponseDto response = warehouseManagerService.getByEmail(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseManagerResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseManagerUpdateDto dto) {
        WarehouseManagerResponseDto response = warehouseManagerService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<WarehouseManagerResponseDto> delete(@PathVariable Long id) {
        WarehouseManagerResponseDto response = warehouseManagerService.delete(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<WarehouseManagerResponseDto> deactivate(@PathVariable Long id) {
        WarehouseManagerResponseDto response = warehouseManagerService.deactivate(id);
        return ResponseEntity.ok(response);
    }
}