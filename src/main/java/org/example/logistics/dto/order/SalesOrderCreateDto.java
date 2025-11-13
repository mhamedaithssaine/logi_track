package org.example.logistics.dto.order;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SalesOrderCreateDto {
    private Long clientId;
    private Long warehouseId;
    private List<OrderLineDto> lines;

    @Data
    @Builder
    public static class OrderLineDto {
        private String sku;
        private Integer quantity;
    }
}
