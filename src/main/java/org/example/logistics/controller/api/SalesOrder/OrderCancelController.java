package org.example.logistics.controller.api.SalesOrder;

import org.example.logistics.dto.order.OrderCancelDto;
import org.example.logistics.dto.order.OrderCancelResponseDto;
import org.example.logistics.service.OrderCancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderCancelController {
    @Autowired
    private OrderCancelService orderCancelService;

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderCancelResponseDto> cancelOrder(@PathVariable Long id) {
        OrderCancelDto dto = OrderCancelDto.builder()
                .orderId(id)
                .build();

        OrderCancelResponseDto response = orderCancelService.cancelOrder(dto);
        return ResponseEntity.ok(response);
    }
}