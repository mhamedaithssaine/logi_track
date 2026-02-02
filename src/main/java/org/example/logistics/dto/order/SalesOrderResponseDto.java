package org.example.logistics.dto.order;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SalesOrderResponseDto {
    private Long id;
    private String status;
    private Long clientId;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime createdAt;
    private List<OrderLineResponseDto> lines;

    @Data
    public static class OrderLineResponseDto {
        private String sku;
        private Integer quantity;
        private Integer backorderQty;
        private String productName;
        private Double price;
    }
}
