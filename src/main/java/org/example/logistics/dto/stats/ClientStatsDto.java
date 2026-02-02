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
public class ClientStatsDto {
    private Map<String, Long> ordersByStatus;
    private long shipmentsCount;
}
