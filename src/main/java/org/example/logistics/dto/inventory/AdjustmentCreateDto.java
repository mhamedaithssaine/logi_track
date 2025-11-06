package org.example.logistics.dto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustmentCreateDto {
    @NotNull(message = "Product ID requis")
    private Long productId;

    @NotNull(message = "Warehouse ID requis")
    private Long warehouseId;

    @NotNull(message = "Quantité d'ajustement requise")
    private Integer adjustmentQty;

    @NotBlank(message = "Reason requis pour traçabilité")
    private String reason;
}