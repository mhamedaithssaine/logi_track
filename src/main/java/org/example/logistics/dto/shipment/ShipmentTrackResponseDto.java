package org.example.logistics.dto.shipment;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class ShipmentTrackResponseDto {
    private Long id;
    private String trackingNumber;
    private String status;
    private String carrier;
    private String message;
    private LocalDateTime plannedDeparture;
    private LocalDateTime deliveredAt;
    private List<LineTrackDto> lines;

    @Data
    public static class LineTrackDto {
        private String sku;
        private Integer quantity;
    }
}
