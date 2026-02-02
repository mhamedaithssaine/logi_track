package org.example.logistics.service;

import org.example.logistics.dto.order.SalesOrderResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.mapper.OrderMapper;
import org.example.logistics.repository.ClientRepository;
import org.example.logistics.repository.ProductRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.example.logistics.dto.order.SalesOrderCreateDto;
import org.example.logistics.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private OrderMapper orderMapper;


    public SalesOrderResponseDto createOrder(SalesOrderCreateDto dto) {
        Optional<Client> optClient = clientRepository.findById(dto.getClientId());
        if (optClient.isEmpty()) {
            throw new RuntimeException("Client non trouvé");
        }

        if (dto.getWarehouseId() != null) {
            Optional<Warehouse> optWarehouse = warehouseRepository.findById(dto.getWarehouseId());
            if (optWarehouse.isEmpty()) {
                throw new RuntimeException("Entrepôt source non trouvé");
            }
        }

        dto.getLines().forEach(lineDto -> {
            Optional<Product> optProd = productRepository.findBySku(lineDto.getSku());
            if (optProd.isEmpty() || !optProd.get().getActive()) {
                throw new RuntimeException("Produit inexistant ou inactif : " + lineDto.getSku());
            }
        });

        SalesOrder order = orderMapper.toEntity(dto);
        order.setClient(optClient.get());
        if (dto.getWarehouseId() != null) {
            order.setWarehouse(warehouseRepository.findById(dto.getWarehouseId()).get());
        } else {
            order.setWarehouse(null);
        }

        List<SalesOrderLine> lines = dto.getLines().stream().map(lineDto -> {
            SalesOrderLine line = orderMapper.toLineEntity(lineDto);
            Product prod = productRepository.findBySku(lineDto.getSku()).get();
            line.setProduct(prod);
            line.setPrice(prod.getPrice());
            line.setSalesOrder(order);
            return line;
        }).collect(Collectors.toList());

        order.setLines(lines);

        SalesOrder saved = salesOrderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    public SalesOrderResponseDto getOrderById(Long id) {
        Optional<SalesOrder> opt = salesOrderRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Commande non trouvée");
        }
        return orderMapper.toDto(opt.get());
    }

    public Page<SalesOrderResponseDto> listByClient(Long clientId, String status,
                                                     LocalDateTime dateFrom, LocalDateTime dateTo,
                                                     int page, int size) {
        List<SalesOrder> all = salesOrderRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        List<SalesOrder> filtered = all.stream()
                .filter(o -> status == null || status.isBlank() || o.getStatus().name().equalsIgnoreCase(status))
                .filter(o -> dateFrom == null || !o.getCreatedAt().isBefore(dateFrom))
                .filter(o -> dateTo == null || !o.getCreatedAt().isAfter(dateTo))
                .collect(Collectors.toList());
        int total = filtered.size();
        int start = Math.min(page * size, total);
        int end = Math.min(start + size, total);
        List<SalesOrder> pageContent = start < end ? filtered.subList(start, end) : List.of();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SalesOrder> orderPage = new PageImpl<>(pageContent, pageable, total);
        return orderPage.map(orderMapper::toDto);
    }

    public SalesOrderResponseDto confirmByAdmin(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("Commande", id));
        if (order.getStatus() != Status.CREATED) {
            throw new IllegalStateException("Seules les commandes au statut CREATED peuvent être confirmées. Statut actuel : " + order.getStatus());
        }
        order.setStatus(Status.CONFIRMED);
        SalesOrder saved = salesOrderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    public Page<SalesOrderResponseDto> listAll(String status, LocalDateTime dateFrom, LocalDateTime dateTo, int page, int size) {
        List<SalesOrder> all = salesOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return filterAndPage(all, status, dateFrom, dateTo, page, size);
    }

    public Page<SalesOrderResponseDto> listByWarehouse(Long warehouseId, String status,
                                                       LocalDateTime dateFrom, LocalDateTime dateTo,
                                                       int page, int size) {
        List<SalesOrder> all = salesOrderRepository.findByWarehouseIdOrUnassignedOrderByCreatedAtDesc(warehouseId);
        return filterAndPage(all, status, dateFrom, dateTo, page, size);
    }

    public SalesOrderResponseDto assignWarehouse(Long orderId, Long warehouseId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.withId("Commande", orderId));
        if (order.getStatus() != Status.CREATED && order.getStatus() != Status.CONFIRMED) {
            throw new IllegalStateException("Seules les commandes CREATED ou CONFIRMED peuvent recevoir un entrepôt.");
        }
        if (order.getWarehouse() != null) {
            throw new IllegalStateException("Cette commande a déjà un entrepôt assigné.");
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> ResourceNotFoundException.withId("Entrepôt", warehouseId));
        order.setWarehouse(warehouse);
        SalesOrder saved = salesOrderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    private Page<SalesOrderResponseDto> filterAndPage(List<SalesOrder> all, String status,
                                                      LocalDateTime dateFrom, LocalDateTime dateTo,
                                                      int page, int size) {
        List<SalesOrder> filtered = all.stream()
                .filter(o -> status == null || status.isBlank() || o.getStatus().name().equalsIgnoreCase(status))
                .filter(o -> dateFrom == null || !o.getCreatedAt().isBefore(dateFrom))
                .filter(o -> dateTo == null || !o.getCreatedAt().isAfter(dateTo))
                .collect(Collectors.toList());
        int total = filtered.size();
        int start = Math.min(page * size, total);
        int end = Math.min(start + size, total);
        List<SalesOrder> pageContent = start < end ? filtered.subList(start, end) : List.of();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PageImpl<>(pageContent, pageable, total).map(orderMapper::toDto);
    }
}