package org.example.logistics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.example.logistics.dto.inventory.OutboundCreateDto;
import org.example.logistics.dto.inventory.OutboundResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.InventoryMovement;
import org.example.logistics.entity.Enum.MovementType;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface OutboundMapper {
    OutboundMapper INSTANCE = Mappers.getMapper(OutboundMapper.class);

    @Mapping(target = "type", constant = "OUTBOUND")
    @Mapping(target = "occurredAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "referenceDoc", source = "referenceDoc")
    InventoryMovement toMovement(OutboundCreateDto dto);

    @Mapping(target = "type", constant = "OUTBOUND")
    @Mapping(target = "quantitySubtracted", expression = "java(originalDto.getQuantity())")
    @Mapping(target = "newQtyOnHand", source = "entity.qtyOnHand")
    @Mapping(target = "message", constant = "Sortie enregistrée")
    OutboundResponseDto toDto(Inventory entity, OutboundCreateDto originalDto);
}