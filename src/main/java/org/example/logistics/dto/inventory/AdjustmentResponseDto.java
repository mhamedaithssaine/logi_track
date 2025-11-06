package org.example.logistics.dto.inventory;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdjustmentResponseDto {
    private Long id;

    private String type;

    private Integer adjustmentApplied;

    private Integer newQtyOnHand;

    private String message;

    private LocalDateTime occurredAt;
}