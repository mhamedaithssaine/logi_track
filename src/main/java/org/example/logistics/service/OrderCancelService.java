package org.example.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logistics.dto.order.OrderCancelDto;
import org.example.logistics.dto.order.OrderCancelResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.OrderCancelMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCancelService {

    private final SalesOrderRepository salesOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderCancelMapper orderCancelMapper;

    private static final List<Status> CANCELABLE_STATUSES = List.of(
            Status.CREATED, Status.RESERVED, Status.PARTIAL_RESERVED
    );

    @Transactional
    public OrderCancelResponseDto cancelOrder(OrderCancelDto dto) {
        log.info("Début annulation commande ID: {}", dto.getOrderId());

        SalesOrder order = fetchCancelableOrder(dto.getOrderId());

        Status previousStatus = order.getStatus();

        int totalStockFreed = 0;
        int linesCanceled = 0;

        // Libérer le stock réservé
        if (order.getLines() != null && !order.getLines().isEmpty()) {
            for (SalesOrderLine line : order.getLines()) {
                int freed = freeReservedStock(line, order.getWarehouse().getId());
                totalStockFreed += freed;
                linesCanceled++;

                if (line.getBackorderQty() != null) {
                    line.setBackorderQty(0);
                }
            }
        }

        // Mise à jour du statut
        order.setStatus(Status.CANCELED);
        order.setCanceledAt(LocalDateTime.now());
        SalesOrder savedOrder = salesOrderRepository.save(order);

        // Mapper la réponse
        OrderCancelResponseDto response = orderCancelMapper.toDto(savedOrder);
        response.setPreviousStatus(previousStatus.name());
        response.setStockFreed(totalStockFreed);
        response.setLinesCanceled(linesCanceled);
        response.setMessage(String.format(
                "Commande #%d annulée avec succès. %d unités restaurées en stock sur %d lignes.",
                savedOrder.getId(), totalStockFreed, linesCanceled
        ));

        log.info("Commande ID: {} annulée avec succès", savedOrder.getId());
        return response;
    }

    private SalesOrder fetchCancelableOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée: " + orderId));

        if (!CANCELABLE_STATUSES.contains(order.getStatus())) {
            throw new IllegalStateException(String.format(
                    "Impossible d'annuler la commande #%d : statut actuel = %s. " +
                            "Seules %s sont annulables.",
                    orderId, order.getStatus(), CANCELABLE_STATUSES
            ));
        }

        return order;
    }

    private int freeReservedStock(SalesOrderLine line, Long warehouseId) {
        if (line.getQuantity() == null || line.getQuantity() <= 0) return 0;

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(line.getProduct().getId(), warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Inventaire introuvable pour produit %d, entrepôt %d",
                                line.getProduct().getId(), warehouseId)
                ));

        int currentReserved = inventory.getQtyReserved() != null ? inventory.getQtyReserved() : 0;
        int quantityToFree = line.getQuantity();
        int newReserved = Math.max(0, currentReserved - quantityToFree);

        inventory.setQtyReserved(newReserved);
        inventoryRepository.save(inventory);

        return quantityToFree;
    }
}
