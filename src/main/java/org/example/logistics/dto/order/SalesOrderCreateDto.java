package org.example.logistics.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SalesOrderCreateDto {

    @NotNull(message = "Client ID est obligatoire")
    private Long clientId;

    /** Optionnel : le manager assigne l'entrepôt après création */
    private Long warehouseId;

    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    @Valid
    private List<OrderLineDto> lines;

    @Data
    @Builder
    public static class OrderLineDto {

        @NotBlank(message = "SKU est obligatoire")
        private String sku;

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être au moins 1")
        private Integer quantity;
    }
}
