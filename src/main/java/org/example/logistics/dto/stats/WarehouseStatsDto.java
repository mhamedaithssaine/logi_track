package org.example.logistics.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseStatsDto {
    private long productsCount;
    private long suppliersCount;
    private long warehousesCount;
    private long ordersToReserveCount;
    private long shipmentsPlannedCount;
    private Map<String, Long> ordersByStatus;
}
