package org.example.logistics.mapper;

import org.example.logistics.dto.order.OrderCancelDto;
import org.example.logistics.dto.order.OrderCancelResponseDto;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderCancelMapper {

    @Mapping(target = "status", ignore = true)
    SalesOrder toEntity(OrderCancelDto dto);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "currentStatus", source = "status")
    @Mapping(target = "previousStatus", ignore = true)
    @Mapping(target = "stockFreed", ignore = true)
    @Mapping(target = "linesCanceled", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "details", source = "lines", qualifiedByName = "mapLineDetails")
    @Mapping(target = "canceledAt", source = "canceledAt")
    OrderCancelResponseDto toDto(SalesOrder order);

    @Named("mapLineDetails")
    default List<OrderCancelResponseDto.CanceledLineDetail> mapLineDetails(List<SalesOrderLine> lines) {
        if (lines == null || lines.isEmpty()) return List.of();
        return lines.stream()
                .map(line -> OrderCancelResponseDto.CanceledLineDetail.builder()
                        .productId(line.getProduct() != null ? line.getProduct().getId() : null)
                        .productName(line.getProduct() != null ? line.getProduct().getName() : "Produit inconnu")
                        .quantityFreed(line.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }
}
