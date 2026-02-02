package org.example.logistics.controller.api.carrier;

import jakarta.validation.Valid;
import org.example.logistics.dto.carrier.CarrierCreateDto;
import org.example.logistics.dto.carrier.CarrierResponseDto;
import org.example.logistics.dto.carrier.CarrierUpdateDto;
import org.example.logistics.service.CarrierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriers")
public class CarrierController {

    @Autowired
    private CarrierService carrierService;

    @GetMapping
    public ResponseEntity<List<CarrierResponseDto>> list(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(carrierService.findAll(activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarrierResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(carrierService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CarrierResponseDto> create(@Valid @RequestBody CarrierCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrierService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarrierResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CarrierUpdateDto dto) {
        return ResponseEntity.ok(carrierService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carrierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
