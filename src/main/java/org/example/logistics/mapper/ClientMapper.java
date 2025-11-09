package org.example.logistics.mapper;


import org.example.logistics.dto.client.ClientCreateDto;
import org.example.logistics.dto.client.ClientRegisterDto;
import org.example.logistics.dto.client.ClientResponseDto;
import org.example.logistics.dto.client.ClientUpdateDto;
import org.example.logistics.entity.Client;
import org.example.logistics.entity.Enum.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(target = "role", constant = "CLIENT")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "passwordHash", ignore = true)
    Client toEntity(ClientRegisterDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    void updateEntityFromDto(ClientUpdateDto dto, @MappingTarget Client entity);

    @Mapping(target = "message", ignore = true)
    ClientResponseDto toDto(Client entity);
}