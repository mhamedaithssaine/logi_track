package org.example.logistics.controller.api.purchase;

import jakarta.validation.Valid;
import org.example.logistics.dto.purchase.PurchaseOrderCreateDto;
import org.example.logistics.dto.purchase.PurchaseOrderReceiveDto;
import org.example.logistics.dto.purchase.PurchaseOrderResponseDto;
import org.example.logistics.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponseDto> createPurchaseOrder(@Valid @RequestBody PurchaseOrderCreateDto dto) {
        PurchaseOrderResponseDto response = purchaseOrderService.createPurchaseOrder(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponseDto> receivePurchaseOrder(@PathVariable("id") Long id, @Valid @RequestBody PurchaseOrderReceiveDto dto) {
        PurchaseOrderResponseDto response = purchaseOrderService.receivePurchaseOrder(id, dto);
        return ResponseEntity.ok(response);
    }

}
