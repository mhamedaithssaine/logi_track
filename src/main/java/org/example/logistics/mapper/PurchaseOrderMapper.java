package org.example.logistics.mapper;

import org.example.logistics.dto.purchase.PurchaseOrderCreateDto;
import org.example.logistics.dto.purchase.PurchaseOrderReceiveDto;
import org.example.logistics.dto.purchase.PurchaseOrderResponseDto;
import org.example.logistics.entity.PurchaseOrder;
import org.example.logistics.entity.PurchaseOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {
    PurchaseOrderMapper INSTANCE = Mappers.getMapper(PurchaseOrderMapper.class);

    @Mapping(target = "status", constant = "APPROVED")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lines", source = "lines")
    PurchaseOrder toEntity(PurchaseOrderCreateDto dto);


    PurchaseOrderLine toLineEntity(PurchaseOrderCreateDto.LineCreateDto lineDto);

    @Mapping(target = "receivedQty", source = "receivedQuantity")
    void updateLineFromReceive(PurchaseOrderReceiveDto.LineReceiveDto lineDto, @MappingTarget PurchaseOrderLine entity);


    @Mapping(source = "status", target = "status")
    @Mapping(source = "lines", target = "lines")
    PurchaseOrderResponseDto toDto(PurchaseOrder entity);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "receivedQty", target = "receivedQuantity")
    PurchaseOrderResponseDto.LineResponseDto toLineDto(PurchaseOrderLine line);
}
