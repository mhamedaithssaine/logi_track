package org.example.logistics.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponseDto {
    private Long id;
    private String sku;
    private String name;
    private String category;
    private boolean active;
    private Double price ;

}
