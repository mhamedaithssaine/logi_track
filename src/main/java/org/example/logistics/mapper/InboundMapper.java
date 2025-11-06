package org.example.logistics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.example.logistics.dto.inventory.InboundCreateDto;
import org.example.logistics.dto.inventory.InboundResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.InventoryMovement;
import org.example.logistics.entity.Enum.MovementType;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface InboundMapper {
    InboundMapper INSTANCE = Mappers.getMapper(InboundMapper.class);

    @Mapping(target = "type", constant = "INBOUND")
    @Mapping(target = "occurredAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "referenceDoc", source = "referenceDoc")
    InventoryMovement toMovement(InboundCreateDto dto);

    @Mapping(target = "type", constant = "INBOUND")
    @Mapping(target = "quantityAdded", constant = "10")
    @Mapping(target = "newQtyOnHand", source = "qtyOnHand")
    @Mapping(target = "message", constant = "Réception enregistrée")
    InboundResponseDto toDto(Inventory entity);
}