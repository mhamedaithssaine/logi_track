package org.example.logistics.dto.shipment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShipmentUpdateResponseDto {
    private Long id ;
    private String status;
    private String message ;
    private String trackingNumber;
}
