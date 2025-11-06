package org.example.logistics.controller.api.SalesOrder;

import jakarta.validation.Valid;
import org.example.logistics.dto.order.SalesOrderCreateDto;
import org.example.logistics.dto.order.SalesOrderResponseDto;
import org.example.logistics.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;


    // creat order
    @PostMapping
    public ResponseEntity<SalesOrderResponseDto> createOrder(@Valid @RequestBody SalesOrderCreateDto dto) {
        SalesOrderResponseDto response = orderService.createOrder(dto);
        return ResponseEntity.ok(response);
    }

    // get order by id
    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponseDto> getOrderById(@PathVariable Long id) {
        SalesOrderResponseDto response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }
}
