package org.example.logistics.dto.warehousemanager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseManagerResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Boolean active;
    private Long warehouseId;
    private String warehouseName;
    private String message;
}