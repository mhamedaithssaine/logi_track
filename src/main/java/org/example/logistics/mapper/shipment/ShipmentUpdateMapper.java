package org.example.logistics.mapper.shipment;

import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.example.logistics.entity.Shipment;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ShipmentUpdateMapper {

    @Mapping(target = "shipmentId", source = "id")
    @Mapping(target = "orderId", source = "salesOrder.id")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "message", constant = "Mise à jour expédition")
    @Mapping(target = "lines", expression = "java(mapLines(entity.getSalesOrder()))")
    ShipmentFullResponseDto toDto(Shipment entity);

    default List<ShipmentFullResponseDto.LineInfo> mapLines(SalesOrder order) {
        if (order == null || order.getLines() == null) {
            return List.of();
        }
        return order.getLines().stream()
                .map(this::toLineInfo)
                .collect(Collectors.toList());
    }

    default ShipmentFullResponseDto.LineInfo toLineInfo(SalesOrderLine line) {
        if (line == null || line.getProduct() == null) return null;
        return ShipmentFullResponseDto.LineInfo.builder()
                .sku(line.getProduct().getSku())
                .quantity(line.getQuantity())
                .build();
    }
}
