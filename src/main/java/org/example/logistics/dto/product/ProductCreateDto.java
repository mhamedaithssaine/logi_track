package org.example.logistics.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductCreateDto {
    @NotBlank(message = "SKU requis")
    private String sku;

    @NotBlank(message = "Name requis")
    private String name;

    @NotBlank(message = "Category requise")
    private String category;

    @NotNull(message = "Price requis")
    private Double price;

    private Boolean active = true;
}
