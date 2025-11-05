package org.example.logistics.dto.order;

import lombok.Data;

import java.util.List;

@Data
public class SalesOrderCreateDto {
    private Long clientId;
    private Long warehouseId;
    private List<OrderLineDto> lines;

    @Data
    public static class OrderLineDto {
        private String sku;
        private Integer quantity;
    }
}
