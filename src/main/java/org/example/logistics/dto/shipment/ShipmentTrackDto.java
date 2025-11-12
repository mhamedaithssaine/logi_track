package org.example.logistics.dto.shipment;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShipmentTrackDto {
    @NotNull(message = "Order ID requis")
    private Long orderId;
}
