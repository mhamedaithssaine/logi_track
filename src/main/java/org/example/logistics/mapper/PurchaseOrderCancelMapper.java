package org.example.logistics.mapper;

import org.example.logistics.dto.purchase.PurchaseOrderCancelResponseDto;
import org.example.logistics.entity.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PurchaseOrderCancelMapper {


    @Mapping(target = "id", source = "id")
    @Mapping(target = "currentStatus", source = "status")
    @Mapping(target = "previousStatus", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "totalLines", source = "lines", qualifiedByName = "countLines")
    @Mapping(target = "supplierName", source = "supplier.name")
    PurchaseOrderCancelResponseDto toDto(PurchaseOrder purchaseOrder);

    @Named("countLines")
    default Integer countLines(java.util.List<?> lines) {
        return lines != null ? lines.size() : 0;
    }
}