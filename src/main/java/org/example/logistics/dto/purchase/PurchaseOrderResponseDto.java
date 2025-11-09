package org.example.logistics.dto.purchase;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PurchaseOrderResponseDto {
    private Long id;
    private String status;
    private List<LineResponseDto> lines;
    private String message;

    @Data
    @Builder
    public static class LineResponseDto {
        private Long productId;
        private Integer quantity;
        private Integer receivedQuantity;
        private String productName;
    }
}
