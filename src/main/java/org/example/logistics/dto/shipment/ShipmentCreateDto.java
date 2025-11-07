package org.example.logistics.dto.shipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipmentCreateDto {
    @NotNull(message = "Order ID requis")
    private Long orderId;

    @NotBlank(message = "Carrier requis")
    private String carrier;

    @NotBlank(message = "Tracking number requis")
    private String trackingNumber;
}
