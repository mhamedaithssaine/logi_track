package org.example.logistics.service;

import org.example.logistics.dto.order.SalesOrderReserveDto;
import org.example.logistics.dto.order.SalesOrderReserveResponseDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.entity.SalesOrderLine;
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
        Optional<SalesOrder> optOrder = salesOrderRepository.findById(dto.getOrderId());
        if (optOrder.isEmpty()) {
            System.out.println("Commande ID " + dto.getOrderId() + " non trouvée en DB");
            throw new RuntimeException("Commande non trouvée ou non créée");
        }

        SalesOrder order = optOrder.get();
        System.out.println("Commande trouvée : ID=" + order.getId() + ", Status=" + order.getStatus());

        if (order.getStatus() != Status.CREATED) {
            System.out.println("Status non CREATED : " + order.getStatus());  // Debug
            throw new RuntimeException("Commande non trouvée ou non créée");
        }


        String message = "Stock réservé";
        boolean partial = false;

        for (SalesOrderLine line : order.getLines()) {
            Optional<Inventory> optInv = inventoryRepository.findByProductIdAndWarehouseId(
                    line.getProduct().getId(), order.getWarehouse().getId());
            if (optInv.isEmpty()) {
                System.out.println("Inventory non trouvé pour product " + line.getProduct().getSku() + ", warehouse " + order.getWarehouse().getId());
                throw new RuntimeException("Inventaire non trouvé pour produit " + line.getProduct().getSku());
            }

            Inventory inv = optInv.get();
            int available = inv.getAvailable();
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
