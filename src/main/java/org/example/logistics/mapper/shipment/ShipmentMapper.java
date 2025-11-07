package org.example.logistics.mapper.shipment;

import org.example.logistics.dto.shipment.ShipmentCreateDto;
import org.example.logistics.dto.shipment.ShipmentResponseDto;
import org.example.logistics.entity.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    ShipmentMapper INSTANCE = Mappers.getMapper(ShipmentMapper.class);

    @Mapping(target = "status", constant = "PLANNED")
    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "plannedDeparture", ignore = true)
    Shipment toEntity(ShipmentCreateDto dto);

    @Mapping(source = "status", target = "status")
    @Mapping(target = "message", constant = "Expédition créée et planifiée")
    ShipmentResponseDto toDto(Shipment entity);

    default LocalDateTime getPlannedDeparture(ShipmentCreateDto dto) {
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() > 15) {
            return now.plusDays(1).withHour(9);
        }
        return now.plusDays(1).withHour(9);
    }
}
