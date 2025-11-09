package org.example.logistics.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierUpdateDto {

    @NotBlank(message = "Name requis")
    private String name;

    private String contact;

    private Long warehouseId;
}
