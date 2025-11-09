package org.example.logistics.mapper;

import org.example.logistics.dto.warehousemanager.WarehouseManagerRegisterDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerResponseDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerUpdateDto;
import org.example.logistics.entity.WarehouseManager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WarehouseManagerMapper {

    WarehouseManagerMapper INSTANCE = Mappers.getMapper(WarehouseManagerMapper.class);

    @Mapping(target = "role", constant = "WAREHOUSE_MANAGER")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    WarehouseManager toEntity(WarehouseManagerRegisterDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    void updateEntityFromDto(WarehouseManagerUpdateDto dto, @MappingTarget WarehouseManager entity);

    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "message", ignore = true)
    WarehouseManagerResponseDto toDto(WarehouseManager entity);
}