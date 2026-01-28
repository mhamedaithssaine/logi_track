package org.example.logistics.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesOrderReserveDto {

    @NotNull(message = "Order ID est obligatoire")
    private Long orderId;
}
