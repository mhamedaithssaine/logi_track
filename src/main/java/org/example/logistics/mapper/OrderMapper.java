package org.example.logistics.mapper;


import lombok.Data;
import org.example.logistics.dto.order.SalesOrderCreateDto;
import org.example.logistics.dto.order.SalesOrderResponseDto;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")

public interface OrderMapper {


    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);


    @Mapping(target = "client", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lines", source = "lines")
    SalesOrder toEntity(SalesOrderCreateDto dto);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "price", ignore = true)
    SalesOrderLine toLineEntity(SalesOrderCreateDto.OrderLineDto lineDto);


    @Mapping(source = "status", target = "status")
    @Mapping(source = "lines", target = "lines")
    SalesOrderResponseDto toDto(SalesOrder entity);


    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.price", target = "price")
    @Mapping(source = "product.sku", target = "sku")
    SalesOrderResponseDto.OrderLineResponseDto toLineDto(SalesOrderLine line);
}
