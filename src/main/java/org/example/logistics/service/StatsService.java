package org.example.logistics.service;

import org.example.logistics.dto.stats.AdminStatsDto;
import org.example.logistics.dto.stats.ClientStatsDto;
import org.example.logistics.dto.stats.WarehouseStatsDto;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.SalesOrder;
import org.example.logistics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    public AdminStatsDto getAdminStats() {
        long productsCount = productRepository.count();
        long suppliersCount = supplierRepository.count();
        long warehousesCount = warehouseRepository.count();
        long ordersCount = salesOrderRepository.count();
        long shipmentsCount = shipmentRepository.count();
        long purchaseOrdersCount = purchaseOrderRepository.count();

        Map<String, Long> ordersByStatus = Arrays.stream(Status.values())
                .filter(s -> !s.name().equals("APPROVED") && !s.name().equals("RECEIVED"))
                .collect(Collectors.toMap(Enum::name, salesOrderRepository::countByStatus));

        return AdminStatsDto.builder()
                .productsCount(productsCount)
                .suppliersCount(suppliersCount)
                .warehousesCount(warehousesCount)
                .ordersCount(ordersCount)
                .ordersByStatus(ordersByStatus)
                .shipmentsCount(shipmentsCount)
                .purchaseOrdersCount(purchaseOrdersCount)
                .build();
    }

    public WarehouseStatsDto getWarehouseStats(Long warehouseId) {
        long productsCount = productRepository.count();
        long suppliersCount = supplierRepository.count();
        long warehousesCount = warehouseRepository.count();

        List<SalesOrder> orders = warehouseId != null
                ? salesOrderRepository.findByWarehouseIdOrUnassignedOrderByCreatedAtDesc(warehouseId)
                : salesOrderRepository.findAll();
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));

        long ordersToReserveCount = orders.stream()
                .filter(o -> o.getStatus() == Status.CREATED || o.getStatus() == Status.CONFIRMED)
                .count();
        long shipmentsPlannedCount = shipmentRepository.count();

        return WarehouseStatsDto.builder()
                .productsCount(productsCount)
                .suppliersCount(suppliersCount)
                .warehousesCount(warehousesCount)
                .ordersToReserveCount(ordersToReserveCount)
                .shipmentsPlannedCount(shipmentsPlannedCount)
                .ordersByStatus(ordersByStatus)
                .build();
    }

    public ClientStatsDto getClientStats(Long clientId) {
        if (clientId == null) {
            return ClientStatsDto.builder()
                    .ordersByStatus(Map.of())
                    .shipmentsCount(0L)
                    .build();
        }
        List<SalesOrder> orders = salesOrderRepository.findByClientId(clientId);
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));
        long shipmentsCount = shipmentRepository.countBySalesOrder_Client_Id(clientId);
        return ClientStatsDto.builder()
                .ordersByStatus(ordersByStatus)
                .shipmentsCount(shipmentsCount)
                .build();
    }
}
