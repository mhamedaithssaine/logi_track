package org.example.logistics.dto.inventory;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutboundResponseDto {

    private Long id;
    private String type;
    private Integer quantitySubtracted;
    private Integer newQtyOnHand;
    private String message;
    private LocalDateTime occurredAt;
}