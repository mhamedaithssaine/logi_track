package org.example.logistics.mapper;

import org.example.logistics.dto.supplier.SupplierCreateDto;
import org.example.logistics.dto.supplier.SupplierUpdateDto;
import org.example.logistics.dto.supplier.SupplierResponseDto;
import org.example.logistics.entity.Supplier;
import org.example.logistics.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    Supplier toEntity(SupplierCreateDto dto);

    @Mapping(target = "id", ignore = true)
    Supplier toEntityFromUpdate(SupplierUpdateDto dto, @MappingTarget Supplier entity);

    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "message", constant = "Fournisseur trouvé")
    SupplierResponseDto toDto(Supplier entity);

    default Warehouse getWarehouse(Supplier supplier) {
        return supplier.getWarehouse();
    }
}