package org.example.logistics.controller.api.stats;

import org.example.logistics.dto.stats.AdminStatsDto;
import org.example.logistics.dto.stats.ClientStatsDto;
import org.example.logistics.dto.stats.WarehouseStatsDto;
import org.example.logistics.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/admin")
    public ResponseEntity<AdminStatsDto> getAdminStats() {
        return ResponseEntity.ok(statsService.getAdminStats());
    }

    @GetMapping("/warehouse")
    public ResponseEntity<WarehouseStatsDto> getWarehouseStats(
            @RequestParam(required = false) Long warehouseId) {
        return ResponseEntity.ok(statsService.getWarehouseStats(warehouseId));
    }

    @GetMapping("/client")
    public ResponseEntity<ClientStatsDto> getClientStats(
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(statsService.getClientStats(clientId));
    }
}
