package org.example.logistics.controller.api.SalesOrder;

import jakarta.validation.Valid;
import org.example.logistics.dto.order.AssignWarehouseDto;
import org.example.logistics.dto.order.SalesOrderCreateDto;
import org.example.logistics.dto.order.SalesOrderResponseDto;
import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.service.OrderService;
import org.example.logistics.service.shipment.ShipmentTrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ShipmentTrackService shipmentTrackService;

    @PostMapping
    public ResponseEntity<SalesOrderResponseDto> createOrder(@Valid @RequestBody SalesOrderCreateDto dto) {
        SalesOrderResponseDto response = orderService.createOrder(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponseDto> getOrderById(@PathVariable Long id) {
        SalesOrderResponseDto response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/shipment")
    public ResponseEntity<ShipmentFullResponseDto> getShipmentByOrderId(@PathVariable Long id) {
        ShipmentFullResponseDto response = shipmentTrackService.getByOrderId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SalesOrderResponseDto>> listOrders(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SalesOrderResponseDto> result;
        if (clientId != null) {
            result = orderService.listByClient(clientId, status, dateFrom, dateTo, page, size);
        } else if (warehouseId != null) {
            result = orderService.listByWarehouse(warehouseId, status, dateFrom, dateTo, page, size);
        } else {
            result = orderService.listAll(status, dateFrom, dateTo, page, size);
        }
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<SalesOrderResponseDto> confirmOrder(@PathVariable Long id) {
        SalesOrderResponseDto response = orderService.confirmByAdmin(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/assign-warehouse")
    public ResponseEntity<SalesOrderResponseDto> assignWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody AssignWarehouseDto dto) {
        SalesOrderResponseDto response = orderService.assignWarehouse(id, dto.getWarehouseId());
        return ResponseEntity.ok(response);
    }
}
