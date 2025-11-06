package org.example.logistics.controller.api.inventory;

import org.example.logistics.dto.inventory.AdjustmentCreateDto;
import org.example.logistics.dto.inventory.AdjustmentResponseDto;
import org.example.logistics.service.AdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class AdjustmentController {
    @Autowired
    private AdjustmentService adjustmentService;

    @PostMapping("/adjustment")
    public ResponseEntity<AdjustmentResponseDto> adjustStock(@Valid @RequestBody AdjustmentCreateDto dto) {
        AdjustmentResponseDto response = adjustmentService.adjustStock(dto);
        return ResponseEntity.ok(response);
    }
}