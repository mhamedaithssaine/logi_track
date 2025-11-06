package org.example.logistics.controller.api.inventory;

import org.example.logistics.dto.inventory.InboundCreateDto;
import org.example.logistics.dto.inventory.InboundResponseDto;
import org.example.logistics.service.InboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InboundController {
    @Autowired
    private InboundService inboundService;

    @PostMapping("/inbound")
    public ResponseEntity<InboundResponseDto> recordInbound(@Valid @RequestBody InboundCreateDto dto) {
        InboundResponseDto response = inboundService.recordInbound(dto);
        return ResponseEntity.ok(response);
    }
}