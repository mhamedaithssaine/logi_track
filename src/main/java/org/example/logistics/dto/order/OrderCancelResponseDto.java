package org.example.logistics.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderCancelResponseDto {
    private Long orderId;
    private String previousStatus;
    private String currentStatus;
    private LocalDateTime canceledAt;
    private Integer stockFreed;
    private Integer linesCanceled;
    private String message;

    private List<CanceledLineDetail> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CanceledLineDetail {
        private Long productId;
        private String productName;
        private Integer quantityFreed;
    }
}