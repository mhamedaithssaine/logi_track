package org.example.logistics.dto.shipment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShipmentResponseDto {

    private Long id;

    private String status;

    private String carrier;

    private String trackingNumber;

    private LocalDateTime plannedDeparture;

    private String message;

    private Long orderId;
}
