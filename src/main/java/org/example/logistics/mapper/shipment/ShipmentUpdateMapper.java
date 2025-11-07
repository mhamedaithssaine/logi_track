package org.example.logistics.mapper.shipment;

import org.example.logistics.dto.shipment.ShipmentUpdateDto;
import org.example.logistics.dto.shipment.ShipmentUpdateResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel="spring")
public interface ShipmentUpdateMapper {
    ShipmentUpdateMapper INSTANCE = Mappers.getMapper(ShipmentUpdateMapper.class);


    @Mapping(target = "status", ignore = true)
    SalesOrder toEntity(ShipmentUpdateDto dto);

    @Mapping(source = "status", target = "status")
    @Mapping(target = "message" , constant = "Commande expédiée")
    ShipmentUpdateResponseDto toDto(SalesOrder entity);

    @Mapping(source = "status", target = "status")
    @Mapping(target = "message", constant = "Livraison confirmée")
    ShipmentUpdateResponseDto toShipmentDto(Shipment entity);

    default String getUpdateMessage(Status status) {
        switch (status) {
            case SHIPPED: return "Commande expédiée";
            case DELIVERED: return "Livraison confirmée";
            default: return "Statut inconnu";
        }
    }

}
