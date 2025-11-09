package org.example.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logistics.dto.order.OrderCancelDto;
import org.example.logistics.dto.order.OrderCancelResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.OrderCancelMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCancelService {

    @Autowired
    private  SalesOrderRepository salesOrderRepository;
    @Autowired
    private  InventoryRepository inventoryRepository;
    @Autowired
    private  OrderCancelMapper orderCancelMapper;

    @Transactional
    public OrderCancelResponseDto cancelOrder(OrderCancelDto dto) {
        log.info("Début annulation commande ID: {}", dto.getOrderId());

        SalesOrder order = fetchCancelableOrder(dto.getOrderId());

        Status previousStatus = order.getStatus();

        int totalStockFreed = 0;
        int linesCanceled = 0;

        if (order.getLines() != null && !order.getLines().isEmpty()) {
            for (SalesOrderLine line : order.getLines()) {
                try {
                    int freed = freeReservedStock(line, order.getWarehouse().getId());
                    totalStockFreed += freed;
                    linesCanceled++;

                    if (line.getBackorderQty() != null) {
                        line.setBackorderQty(0);
                    }

                    log.info("Ligne annulée - Produit ID: {}, Quantité libérée: {}",
                            line.getProduct().getId(), freed);
                } catch (Exception e) {
                    log.error("Erreur libération stock pour Produit ID: {}",
                            line.getProduct().getId(), e);
                }
            }
        }

        // 3. Mettre à jour le statut de la commande
        order.setStatus(Status.CANCELED);
        order.setCanceledAt(LocalDateTime.now());
        SalesOrder savedOrder = salesOrderRepository.save(order);

        log.info("Commande ID: {} annulée. Stock libéré: {} unités, Lignes: {}",
                savedOrder.getId(), totalStockFreed, linesCanceled);

        // 4. Construire
        OrderCancelResponseDto response = orderCancelMapper.toDto(savedOrder);
        response.setPreviousStatus(previousStatus.name());
        response.setStockFreed(totalStockFreed);
        response.setLinesCanceled(linesCanceled);
        response.setMessage(String.format(
                "Commande #%d annulée avec succès. %d unités restaurées en stock sur %d lignes.",
                savedOrder.getId(), totalStockFreed, linesCanceled
        ));

        return response;
    }


    private SalesOrder fetchCancelableOrder(Long orderId) {
        return salesOrderRepository.findCancelableOrderById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(
                                "La commande #%d n'existe pas ou ne peut pas être annulée. " +
                                        "Seules les commandes CREATED, RESERVED ou PARTIAL_RESERVED peuvent être annulées.",
                                orderId
                        )
                ));

        /* Option 2 : Utiliser findByIdAndStatusIn
        List<Status> allowedStatuses = Arrays.asList(
            Status.CREATED,
            Status.RESERVED,
            Status.PARTIAL_RESERVED
        );
        return salesOrderRepository.findByIdAndStatusIn(orderId, allowedStatuses)
                .orElseThrow(() -> new OrderCannotBeCanceledException(...));
        */

        /* Option 3 : Utiliser findByIdAndStatusNotIn
        List<Status> excludedStatuses = Arrays.asList(
            Status.SHIPPED,
            Status.DELIVERED,
            Status.CANCELED
        );
        return salesOrderRepository.findByIdAndStatusNotIn(orderId, excludedStatuses)
                .orElseThrow(() -> new OrderCannotBeCanceledException(...));
        */
    }


    private int freeReservedStock(SalesOrderLine line, Long warehouseId) {
        if (line.getQuantity() == null || line.getQuantity() <= 0) {
            log.warn("Ligne avec quantité nulle ou négative, ignorée");
            return 0;
        }

        // Récupérer l'inventaire
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(line.getProduct().getId(), warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(
                                "Inventory introuvable pour Produit ID: %d, Warehouse ID: %d",
                                line.getProduct().getId(), warehouseId
                        )
                ));

        int currentReserved = inventory.getQtyReserved() != null ? inventory.getQtyReserved() : 0;
        int quantityToFree = line.getQuantity();

        int newReserved = Math.max(0, currentReserved - quantityToFree);
        inventory.setQtyReserved(newReserved);

        inventoryRepository.save(inventory);

        log.debug("Inventory mis à jour - Produit ID: {}, Ancienne qtyReserved: {}, Nouvelle: {}",
                line.getProduct().getId(), currentReserved, newReserved);

        return quantityToFree;
    }


    @Transactional(readOnly = true)
    public List<SalesOrder> getCanceledOrdersBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return salesOrderRepository.findCanceledOrdersBetween(startDate, endDate);
    }


    @Transactional(readOnly = true)
    public long countCanceledOrders() {
        return salesOrderRepository.countByStatus(Status.CANCELED);
    }
}