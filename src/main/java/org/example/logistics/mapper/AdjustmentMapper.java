package org.example.logistics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.example.logistics.dto.inventory.AdjustmentCreateDto;
import org.example.logistics.dto.inventory.AdjustmentResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.InventoryMovement;
import org.example.logistics.entity.Enum.MovementType;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface AdjustmentMapper {
    AdjustmentMapper INSTANCE = Mappers.getMapper(AdjustmentMapper.class);

    @Mapping(target = "type", constant = "ADJUSTMENT")
    @Mapping(target = "occurredAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "quantity", source = "adjustmentQty")
    @Mapping(target = "referenceDoc", source = "reason")
    InventoryMovement toMovement(AdjustmentCreateDto dto);

    @Mapping(target = "type", constant = "ADJUSTMENT")
    @Mapping(target = "adjustmentApplied", expression = "java(originalDto.getAdjustmentQty())")
    @Mapping(target = "newQtyOnHand", source = "entity.qtyOnHand")
    @Mapping(target = "message", constant = "Ajustement enregistré")
    AdjustmentResponseDto toDto(Inventory entity, AdjustmentCreateDto originalDto);
}