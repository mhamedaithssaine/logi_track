package org.example.logistics.mapper.shipment;

import org.example.logistics.dto.shipment.ShipmentFullResponseDto;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.example.logistics.entity.Shipment;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ShipmentTrackMapper {

    @Mapping(target = "shipmentId", source = "id")
    @Mapping(target = "orderId", source = "salesOrder.id")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "message", constant = "Suivi de l’expédition")
    @Mapping(target = "lines", expression = "java(mapLines(entity.getSalesOrder()))")
    ShipmentFullResponseDto toDto(Shipment entity);

    default List<ShipmentFullResponseDto.LineInfo> mapLines(SalesOrder order) {
        if (order == null || order.getLines() == null) {
            return List.of();
        }
        return order.getLines().stream()
                .map(line -> ShipmentFullResponseDto.LineInfo.builder()
                        .sku(line.getProduct().getSku())
                        .quantity(line.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }
}
