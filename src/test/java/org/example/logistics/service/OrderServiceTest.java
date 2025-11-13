package org.example.logistics.service;

import org.example.logistics.dto.order.SalesOrderCreateDto;
import org.example.logistics.dto.order.SalesOrderResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.mapper.OrderMapper;
import org.example.logistics.repository.ClientRepository;
import org.example.logistics.repository.ProductRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private OrderMapper orderMapper;

    @InjectMocks private OrderService orderService;

    private Client client;
    private Warehouse warehouse;
    private Product product;
    private SalesOrder order;
    private SalesOrderLine line;

    @BeforeEach
    void setup() {
        client = new Client();
        client.setId(1L);

        warehouse = new Warehouse();
        warehouse.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setSku("SKU001");
        product.setActive(true);
        product.setPrice(10.0);

        line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(5);

        order = new SalesOrder();
        order.setId(1L);
        order.setClient(client);
        order.setWarehouse(warehouse);
        order.setLines(new ArrayList<>());
        order.getLines().add(line);
    }

    @Test
    void shouldCreateOrder_success() {
        SalesOrderCreateDto.OrderLineDto lineDto = SalesOrderCreateDto.OrderLineDto.builder()
                .sku(product.getSku())
                .quantity(5)
                .build();

        SalesOrderCreateDto dto = SalesOrderCreateDto.builder()
                .clientId(client.getId())
                .warehouseId(warehouse.getId())
                .lines(List.of(lineDto))
                .build();

        // Mocks
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(warehouse.getId())).thenReturn(Optional.of(warehouse));
        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(product));
        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(orderMapper.toLineEntity(any(SalesOrderCreateDto.OrderLineDto.class))).thenReturn(line);
        when(orderMapper.toDto(order)).thenReturn(
                SalesOrderResponseDto.builder()
                        .id(order.getId())
                        .build()
        );
        when(salesOrderRepository.save(any())).thenReturn(order);

        // Test
        SalesOrderResponseDto response = orderService.createOrder(dto);

        assertNotNull(response);
        assertEquals(order.getId(), response.getId());
        verify(salesOrderRepository, times(1)).save(order);
    }

    @Test
    void shouldThrow_whenClientNotFound() {
        SalesOrderCreateDto dto = SalesOrderCreateDto.builder()
                .clientId(999L)
                .warehouseId(1L)
                .lines(new ArrayList<>())
                .build();

        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(dto));
        assertEquals("Client non trouvé", ex.getMessage());
    }

    @Test
    void shouldThrow_whenWarehouseNotFound() {
        SalesOrderCreateDto dto = SalesOrderCreateDto.builder()
                .clientId(client.getId())
                .warehouseId(999L)
                .lines(new ArrayList<>())
                .build();

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(dto));
        assertEquals("Entrepôt source non trouvé", ex.getMessage());
    }

    @Test
    void shouldThrow_whenProductNotFoundOrInactive() {
        SalesOrderCreateDto.OrderLineDto lineDto = SalesOrderCreateDto.OrderLineDto.builder()
                .sku("SKU-999")
                .quantity(5)
                .build();

        SalesOrderCreateDto dto = SalesOrderCreateDto.builder()
                .clientId(client.getId())
                .warehouseId(warehouse.getId())
                .lines(List.of(lineDto))
                .build();

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(warehouse.getId())).thenReturn(Optional.of(warehouse));
        when(productRepository.findBySku("SKU-999")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(dto));
        assertEquals("Produit inexistant ou inactif : SKU-999", ex.getMessage());
    }

    @Test
    void shouldGetOrderById_success() {
        when(salesOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(
                SalesOrderResponseDto.builder().id(order.getId()).build()
        );

        SalesOrderResponseDto response = orderService.getOrderById(order.getId());

        assertNotNull(response);
        assertEquals(order.getId(), response.getId());
    }

    @Test
    void shouldThrow_whenGetOrderByIdNotFound() {
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L));
        assertEquals("Commande non trouvée", ex.getMessage());
    }
}
