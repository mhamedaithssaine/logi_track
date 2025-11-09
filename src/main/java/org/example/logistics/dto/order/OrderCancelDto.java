package org.example.logistics.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCancelDto {
    @NotNull(message = "L'ID de la commande est obligatoire")
    private Long orderId;

    private String cancelReason;}