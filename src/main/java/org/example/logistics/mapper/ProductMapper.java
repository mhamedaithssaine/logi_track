package org.example.logistics.mapper;

import org.example.logistics.dto.product.ProductResponseDto;
import org.example.logistics.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    ProductResponseDto toDto(Product product);
    Product toEntity(ProductResponseDto dto);
    void updateEntityFromDto(ProductResponseDto dto, @MappingTarget Product entity);

}
