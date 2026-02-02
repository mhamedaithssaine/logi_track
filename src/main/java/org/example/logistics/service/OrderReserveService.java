package org.example.logistics.service;

import lombok.extern.slf4j.Slf4j;
import org.example.logistics.dto.order.SalesOrderReserveDto;
import org.example.logistics.dto.order.SalesOrderReserveResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.OrderReserveMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Slf4j
@Service
public class OrderReserveService {
    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderReserveMapper orderReserveMapper;

    public SalesOrderReserveResponseDto reserveOrder(SalesOrderReserveDto dto) {
        log.info("ORDER_RESERVE_START orderId={}", dto.getOrderId());


        if (dto == null || dto.getOrderId() == null) {
            log.warn("ORDER_RESERVE_FAILED orderId={} reason=ORDER_NOT_FOUND",
                    dto.getOrderId());
            throw new IllegalArgumentException("orderId manquant");
        }

        Optional<SalesOrder> optOrder = salesOrderRepository.findById(dto.getOrderId());
        if (optOrder.isEmpty()) {
            throw  ResourceNotFoundException.withId("Commande non trouvée ou non créée", dto.getOrderId());
        }

        SalesOrder order = optOrder.get();
        System.out.println("Commande trouvée : ID=" + order.getId() + ", Status=" + order.getStatus());

        if (order.getStatus() != Status.CREATED && order.getStatus() != Status.CONFIRMED) {
            log.warn("ORDER_RESERVE_FAILED orderId={} status={} reason=INVALID_STATUS",
                    order.getId(),
                    order.getStatus());
            throw new IllegalStateException("Impossible de réserver : " + order.getId());
        }

        if (order.getWarehouse() == null) {
            log.warn("ORDER_RESERVE_FAILED orderId={} reason=NO_WAREHOUSE_ASSIGNED", order.getId());
            throw new IllegalStateException("Assignez un entrepôt à cette commande avant de réserver.");
        }

        String message = "Stock réservé";
        boolean partial = false;

        for (SalesOrderLine line : order.getLines()) {
            Optional<Inventory> optInv = inventoryRepository.findByProductIdAndWarehouseId(
                    line.getProduct().getId(), order.getWarehouse().getId());
            if (optInv.isEmpty()) {
                String sku = line.getProduct().getSku();
                String warehouseName = order.getWarehouse().getCode() != null ? order.getWarehouse().getCode() : "ID " + order.getWarehouse().getId();
                log.error("ORDER_RESERVE_FAILED orderId={} productSku={} warehouseId={} reason=INVENTORY_NOT_FOUND",
                        order.getId(), sku, order.getWarehouse().getId());
                throw new ConflictException(
                        "Inventaire absent pour le produit " + sku + " dans l'entrepôt " + warehouseName + ". "
                                + "Créez d'abord une réception (INBOUND) pour ce produit dans cet entrepôt.");
            }

            Inventory inv = optInv.get();
            int available = inv.getAvailable();
            if (available <= 0) {
                line.setBackorderQty(line.getQuantity());
                partial = true;
                message = "Aucun stock disponible pour " + line.getProduct().getSku();

                log.warn("ORDER_RESERVE_NO_STOCK orderId={} productSku={} requestedQty={}",
                        order.getId(),
                        line.getProduct().getSku(),
                        line.getQuantity());
            }
            if (available < line.getQuantity()) {
                line.setBackorderQty(line.getQuantity() - available);
                inv.setQtyReserved(inv.getQtyReserved() + available);
                partial = true;
                message = "Réservation partielle : Backorder pour " + line.getBackorderQty() + " unités " + line.getProduct().getSku();
                log.info("ORDER_RESERVE_PARTIAL orderId={} productSku={} reserved={} backorder={}",
                        order.getId(),
                        line.getProduct().getSku(),
                        available,
                        line.getBackorderQty());

            } else {
                line.setBackorderQty(0);
                inv.setQtyReserved(inv.getQtyReserved() + line.getQuantity());
                log.info("ORDER_RESERVE_FULL orderId={} productSku={} qty={}",
                        order.getId(),
                        line.getProduct().getSku(),
                        line.getQuantity());

            }

            inventoryRepository.save(inv);
            line.setSalesOrder(order);
            log.info("INVENTORY_RESERVED productSku={} warehouseId={} reservedQty={} totalReserved={}",
                    line.getProduct().getSku(),
                    order.getWarehouse().getId(),
                    line.getQuantity(),
                    inv.getQtyReserved());

        }

        order.setStatus(partial ? Status.PARTIAL_RESERVED : Status.RESERVED);

        salesOrderRepository.save(order);

        return orderReserveMapper.toDto(order);

    }

}
