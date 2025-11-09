package org.example.logistics.dto.supplier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierResponseDto {
    private Long id;
    private String name;
    private String contact;
    private Long warehouseId;
    private String warehouseName;
    private String message;
}
