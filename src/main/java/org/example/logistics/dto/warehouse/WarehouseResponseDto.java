package org.example.logistics.dto.warehouse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarehouseResponseDto {
    private Long id;
    private String code;
    private String name;
    private String message;
}
