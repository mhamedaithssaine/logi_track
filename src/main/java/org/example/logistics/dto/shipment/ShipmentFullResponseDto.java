package org.example.logistics.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logistics.entity.Enum.Status_shipment;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShipmentFullResponseDto {
    private Long shipmentId;
    private Long orderId;
    private String trackingNumber;
    private String carrier;
    private String status;
    private String message;
    private LocalDateTime plannedDeparture;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private List<LineInfo> lines;

    @Data
    @Builder
    public static class LineInfo {
        private String sku;
        private Integer quantity;
    }
}
