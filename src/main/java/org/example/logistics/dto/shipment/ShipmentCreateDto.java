package org.example.logistics.dto.shipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShipmentCreateDto {
    @NotNull(message = "Order ID requis")
    private Long orderId;

    @NotBlank(message = "Carrier requis")
    private String carrier;

    @NotBlank(message = "Le numéro de suivi est obligatoire")
    private String trackingNumber;

}
