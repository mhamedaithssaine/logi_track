package org.example.logistics.dto.order;

import lombok.Data;

import java.util.List;

@Data
public class SalesOrderResponseDto {
    private Long id;
    private String status;
    private Long clientId;
    private Long warehouseId;
    private List<OrderLineResponseDto> lines;

    @Data
    public static class OrderLineResponseDto {
        private String sku;
        private Integer quantity;
        private String productName;
        private Double price;
    }
}
