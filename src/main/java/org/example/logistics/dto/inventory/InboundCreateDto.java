package org.example.logistics.dto.inventory;


import lombok.Data;

@Data
public class InboundCreateDto {

    private Long productId;
    private Long warehouseId;

    private Integer quantity;

    private String referenceDoc;
}
