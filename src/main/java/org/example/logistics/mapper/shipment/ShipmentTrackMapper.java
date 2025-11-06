package org.example.logistics.mapper.shipment;

import org.example.logistics.dto.shipment.ShipmentTrackDto;
import org.example.logistics.dto.shipment.ShipmentTrackResponseDto;
import org.example.logistics.entity.Enum.Status_shipment;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShipmentTrackMapper {
    ShipmentTrackMapper INSTANCE = Mappers.getMapper(ShipmentTrackMapper.class);


    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", source = "orderId")
    SalesOrder toEntity(ShipmentTrackDto dto);

    @Mapping(source = "status", target = "status")
    @Mapping(target = "message", expression = "java(getStatusMessage(entity.getStatus()))")
    ShipmentTrackResponseDto toDto(Shipment entity);

    default String getStatusMessage(Status_shipment status) {
        switch (status) {
            case PLANNED: return "Expédition planifiée";
            case IN_TRANSIT: return "En transit vers vous";
            case DELIVERED: return "Livraison confirmée";
            default: return "Statut inconnu";
        }
    }
}
