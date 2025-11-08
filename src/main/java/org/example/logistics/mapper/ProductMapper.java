package org.example.logistics.mapper;

import org.example.logistics.dto.product.ProductCreateDto;
import org.example.logistics.dto.product.ProductResponseDto;
import org.example.logistics.dto.product.ProductUpdateDto;
import org.example.logistics.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);


    @Mapping(target = "active", constant = "true")
    Product toEntity(ProductCreateDto dto);

    @Mapping(target = "sku", ignore = true)
    void toEntityFromUpdate(ProductUpdateDto dto, @MappingTarget Product entity);

    @Mapping(target = "message", constant = "Produit trouvé")
    @Mapping(target = "price", defaultExpression = "java(entity.getPrice() != null ? entity.getPrice() : 0.0)")
    @Mapping(target = "active", defaultExpression = "java(entity.getActive() != null ? entity.getActive() : true)")
    ProductResponseDto toDto(Product entity);

    default String getUniqueMessage(boolean exists) {
        return exists ? "SKU existe déjà" : "SKU unique OK";
    }

}
