package org.example.logistics.dto.order;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SalesOrderReserveResponseDto {
    private Long id ;
    private String status;
    private List<LineReserveDto> lines;
    private String message;

    @Data
    public static class  LineReserveDto{
        private Long id;
        private String sku;
        private Integer requestedQty;
        private Integer reservedQty;
        private Integer backorderQty;
        private String productName;
    }
}
