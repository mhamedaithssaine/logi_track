package org.example.logistics.dto.inventory;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InboundResponseDto {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private String type;

    private Integer quantityAdded;

    private Double newQtyOnHand;

    private String message;

    private LocalDateTime occurredAt;
}
