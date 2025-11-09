package org.example.logistics.dto.warehousemanager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseManagerUpdateDto {
    private String name;
    private String phone;
    private Long warehouseId;
}