package org.example.logistics.controller.api.SalesOrder;

import jakarta.validation.Valid;
import org.example.logistics.dto.order.SalesOrderReserveDto;
import org.example.logistics.dto.order.SalesOrderReserveResponseDto;
import org.example.logistics.service.OrderReserveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderReserveController {
    @Autowired
    private OrderReserveService orderReserveService;

    @PostMapping("/{id}/reserve")
    public ResponseEntity<SalesOrderReserveResponseDto> reserveOrder(@PathVariable Long id, @Valid @RequestBody SalesOrderReserveDto dto) {
        dto.setOrderId(id);
        SalesOrderReserveResponseDto response = orderReserveService.reserveOrder(dto);
        return ResponseEntity.ok(response);
    }
}
