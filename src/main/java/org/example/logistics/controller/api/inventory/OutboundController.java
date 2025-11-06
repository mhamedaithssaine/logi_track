package org.example.logistics.controller.api.inventory;
import org.example.logistics.dto.inventory.OutboundCreateDto;
import org.example.logistics.dto.inventory.OutboundResponseDto;
import org.example.logistics.service.OutboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class OutboundController {
    @Autowired
    private OutboundService outboundService;

    @PostMapping("/outbound")
    public ResponseEntity<OutboundResponseDto> recordOutbound(@Valid @RequestBody OutboundCreateDto dto) {
        OutboundResponseDto response = outboundService.recordOutbound(dto);
        return ResponseEntity.ok(response);
    }
}