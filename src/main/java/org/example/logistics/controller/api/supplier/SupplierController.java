package org.example.logistics.controller.api.supplier;

import org.example.logistics.dto.supplier.SupplierCreateDto;
import org.example.logistics.dto.supplier.SupplierResponseDto;
import org.example.logistics.dto.supplier.SupplierUpdateDto;
import org.example.logistics.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public ResponseEntity<List<SupplierResponseDto>> getAllSuppliers() {
        List<SupplierResponseDto> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDto> getSupplierById(@PathVariable Long id) {
        SupplierResponseDto supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @PostMapping
    public ResponseEntity<SupplierResponseDto> createSupplier(@Valid @RequestBody SupplierCreateDto dto) {
        SupplierResponseDto supplier = supplierService.createSupplier(dto);
        return ResponseEntity.ok(supplier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDto> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierUpdateDto dto) {
        SupplierResponseDto supplier = supplierService.updateSupplier(id, dto);
        return ResponseEntity.ok(supplier);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSupplier(@PathVariable Long id) {
        String message = supplierService.deleteSupplier(id);
        return ResponseEntity.ok(message);
    }
}