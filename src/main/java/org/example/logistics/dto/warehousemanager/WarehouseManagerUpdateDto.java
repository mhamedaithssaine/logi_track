package org.example.logistics.dto.warehousemanager;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseManagerUpdateDto {
    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Format de téléphone marocain invalide")
    private String phone;

    @NotNull(message = "L'ID du warehouse est obligatoire")
    private Long warehouseId;
}