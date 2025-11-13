package org.example.logistics.service;

import org.example.logistics.dto.order.SalesOrderReserveDto;
import org.example.logistics.dto.order.SalesOrderReserveResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.OrderReserveMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderReserveService {
    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderReserveMapper orderReserveMapper;

    public SalesOrderReserveResponseDto reserveOrder(SalesOrderReserveDto dto) {

        if (dto == null || dto.getOrderId() == null) {
            throw new IllegalArgumentException("orderId manquant");
        }

        Optional<SalesOrder> optOrder = salesOrderRepository.findById(dto.getOrderId());
        if (optOrder.isEmpty()) {
            throw  ResourceNotFoundException.withId("Commande non trouvée ou non créée", dto.getOrderId());
        }

        SalesOrder order = optOrder.get();
        System.out.println("Commande trouvée : ID=" + order.getId() + ", Status=" + order.getStatus());

        if (order.getStatus() != Status.CREATED) {
            System.out.println("Status non CREATED : " + order.getStatus());
            throw new IllegalStateException("Impossible de réserver : " + order.getId());
        }


        String message = "Stock réservé";
        boolean partial = false;

        for (SalesOrderLine line : order.getLines()) {
            Optional<Inventory> optInv = inventoryRepository.findByProductIdAndWarehouseId(
                    line.getProduct().getId(), order.getWarehouse().getId());
            if (optInv.isEmpty()) {
                System.out.println("Inventory non trouvé pour product " + line.getProduct().getSku() + ", warehouse " + order.getWarehouse().getId());
                throw ResourceNotFoundException.withString("Inventaire non trouvé pour produit " , "sku" , line.getProduct().getSku());
            }

            Inventory inv = optInv.get();
            int available = inv.getAvailable();
            if (available <= 0) {
                line.setBackorderQty(line.getQuantity());
                partial = true;
                message = "Aucun stock disponible pour " + line.getProduct().getSku();
                System.out.println("Aucun stock dispo pour " + line.getProduct().getSku());
            }
            if (available < line.getQuantity()) {
                line.setBackorderQty(line.getQuantity() - available);
                inv.setQtyReserved(inv.getQtyReserved() + available);
                partial = true;
                message = "Réservation partielle : Backorder pour " + line.getBackorderQty() + " unités " + line.getProduct().getSku();
            } else {
                line.setBackorderQty(0);
                inv.setQtyReserved(inv.getQtyReserved() + line.getQuantity());
            }

            inventoryRepository.save(inv);
            line.setSalesOrder(order);
        }

        order.setStatus(partial ? Status.PARTIAL_RESERVED : Status.RESERVED);
        salesOrderRepository.save(order);

        return orderReserveMapper.toDto(order);

    }

}
