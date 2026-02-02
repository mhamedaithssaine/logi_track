package org.example.logistics.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignWarehouseDto {
    @NotNull(message = "Warehouse ID est obligatoire")
    private Long warehouseId;
}
