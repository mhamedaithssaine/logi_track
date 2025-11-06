package org.example.logistics.dto.inventory;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InboundResponseDto {
    private Long id;

    private String type;

    private Integer quantityAdded;

    private Double newQtyOnHand;

    private String message;

    private LocalDateTime occurredAt;
}
