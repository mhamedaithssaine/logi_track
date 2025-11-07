package org.example.logistics.dto.shipment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public  class ShipmentUpdateDto {
    private Long orderId;
    private Long shipmentId;
}
