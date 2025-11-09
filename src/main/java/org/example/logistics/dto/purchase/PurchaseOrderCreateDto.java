package org.example.logistics.dto.purchase;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PurchaseOrderCreateDto {
    @NotNull(message = "Supplier ID requis")
    private Long supplierId;

    @NotEmpty(message = "Au moins une ligne requise")
    private List<LineCreateDto> lines;

    @Data
    @Builder
    public static class LineCreateDto {
        @NotNull(message = "Product ID requis")
        private Long productId;

        @Min(value = 1, message = "Quantité doit être positive")
        private Integer quantity;
    }
}
