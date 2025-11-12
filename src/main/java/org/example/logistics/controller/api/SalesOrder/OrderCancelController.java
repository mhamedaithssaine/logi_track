package org.example.logistics.controller.api.SalesOrder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.logistics.dto.order.OrderCancelDto;
import org.example.logistics.dto.order.OrderCancelResponseDto;
import org.example.logistics.service.OrderCancelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderCancelController {

    private final OrderCancelService orderCancelService;

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderCancelResponseDto> cancelOrder(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) OrderCancelDto dto) {

        if (dto == null) {
            dto = OrderCancelDto.builder().orderId(id).build();
        } else {
            dto.setOrderId(id);
        }

        OrderCancelResponseDto response = orderCancelService.cancelOrder(dto);
        return ResponseEntity.ok(response);
    }
}
