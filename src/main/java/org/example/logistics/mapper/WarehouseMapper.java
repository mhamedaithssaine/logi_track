package org.example.logistics.mapper;

import org.example.logistics.dto.warehouse.WarehouseCreateDto;
import org.example.logistics.dto.warehouse.WarehouseResponseDto;
import org.example.logistics.dto.warehouse.WarehouseUpdateDto;
import org.example.logistics.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    Warehouse toEntity(WarehouseCreateDto dto);

    @Mapping(target = "code", ignore = true)
    Warehouse toEntityFromUpdate(WarehouseUpdateDto dto, @MappingTarget Warehouse entity);

    WarehouseResponseDto toDto(Warehouse entity);

    default String getUniqueMessage(boolean exists) {
        return exists ? "Code entrepôt existe déjà" : "Code unique OK";
    }
}
