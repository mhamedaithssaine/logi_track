package org.example.logistics.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarehouseCreateDto {
    @NotBlank(message = "Code requis")
    private String code;

    @NotBlank(message = "Name requis")
    private String name;
}
