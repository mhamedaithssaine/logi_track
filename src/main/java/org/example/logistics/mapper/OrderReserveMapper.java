package org.example.logistics.mapper;

import org.example.logistics.dto.order.SalesOrderReserveDto;
import org.example.logistics.dto.order.SalesOrderReserveResponseDto;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderReserveMapper {

    OrderReserveMapper  INSTANCE = Mappers.getMapper(OrderReserveMapper.class);

    @Mapping(target = "status" , ignore = true)
    SalesOrder toEntity(SalesOrderReserveDto dto);

    @Mapping(source = "status", target = "status")
    @Mapping(source = "lines", target = "lines")
    @Mapping(target = "message",constant = "Stock réservé")
    SalesOrderReserveResponseDto toDto(SalesOrder entity);

    @Mapping(source = "quantity", target = "requestedQty")
    @Mapping(target = "reservedQty", expression = "java(line.getQuantity() - (line.getBackorderQty() != null ? line.getBackorderQty() : 0))")
    @Mapping(target = "backorderQty", expression = "java(line.getBackorderQty() != null ? line.getBackorderQty() : 0)")
    @Mapping(source = "product.sku",target = "sku")
    @Mapping(source = "product.name", target = "productName")
    SalesOrderReserveResponseDto.LineReserveDto toLineDto(SalesOrderLine line);

}
