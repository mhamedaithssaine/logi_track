package org.example.logistics.dto.purchase;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderCancelDto {

    @NotNull(message = "L'ID du Purchase Order est obligatoire")
    private Long poId;

    private String cancelReason;
}