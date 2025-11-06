package org.example.logistics.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OutboundCreateDto {
    @NotNull(message = "Product ID requis")
    private Long productId;

    @NotNull(message = "Warehouse ID requis")
    private Long warehouseId;

    @Min(value = 1, message = "Quantité doit être positive")
    private Integer quantity;

    @NotBlank(message = "ReferenceDoc requis pour traçabilité")
    private String referenceDoc;
}
