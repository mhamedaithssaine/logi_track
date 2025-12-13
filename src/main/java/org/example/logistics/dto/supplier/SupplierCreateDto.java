package org.example.logistics.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierCreateDto {

    @NotBlank(message = "Name requis")
    private String name;

    @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Format de téléphone marocain invalide")
    private String contact;

    @NotNull(message = "Warehouse ID est obligatoire")
    private Long warehouseId;
}
