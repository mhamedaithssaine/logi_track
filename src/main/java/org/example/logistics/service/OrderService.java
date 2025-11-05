package org.example.logistics.service;

import org.example.logistics.dto.order.SalesOrderCreateDto;
import org.example.logistics.dto.order.SalesOrderResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.mapper.OrderMapper;
import org.example.logistics.repository.ClientRepository;
import org.example.logistics.repository.ProductRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

        Optional<Warehouse> optWarehouse = warehouseRepository.findById(dto.getWarehouseId());
        if (optWarehouse.isEmpty()) {
            throw new RuntimeException("Entrepôt source non trouvé");
        }

        dto.getLines().forEach(lineDto -> {
            Optional<Product> optProd = productRepository.findBySku(lineDto.getSku());
            if (optProd.isEmpty() || !optProd.get().getActive()) {
                throw new RuntimeException("Produit inexistant ou inactif : " + lineDto.getSku());
            }
        });

        SalesOrder order = orderMapper.toEntity(dto);
        order.setClient(optClient.get());
        order.setWarehouse(optWarehouse.get());

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

    // get commande by id
    public SalesOrderResponseDto getOrderById(Long id) {
        Optional<SalesOrder> opt = salesOrderRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Commande non trouvée");
        }
        return orderMapper.toDto(opt.get());
    }
}