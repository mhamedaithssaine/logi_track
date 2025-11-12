package org.example.logistics.dto.shipment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public  class ShipmentUpdateDto {

    @NotNull(message = "Id order est requis")
    private Long orderId;
    @NotNull(message = "Id shipment requise")
    private Long shipmentId;

}
