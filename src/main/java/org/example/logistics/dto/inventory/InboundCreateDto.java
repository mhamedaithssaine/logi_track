package org.example.logistics.dto.inventory;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InboundCreateDto {

    @NotNull(message = "Product ID est obligatoire")
    private Long productId;

    @NotNull(message = "Warehouse ID est obligatoire")
    private Long warehouseId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantity;

    private String referenceDoc;
}
