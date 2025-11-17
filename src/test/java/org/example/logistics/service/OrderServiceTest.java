package org.example.logistics.service;

import org.example.logistics.dto.order.SalesOrderCreateDto;
import org.example.logistics.dto.order.SalesOrderResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.Role;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.mapper.OrderMapper;
import org.example.logistics.repository.ClientRepository;
import org.example.logistics.repository.ProductRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Order Service Tests")
class OrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private SalesOrderCreateDto createDto;
    private Client client;
    private Warehouse warehouse;
    private Product product;
    private SalesOrder salesOrder;
    private SalesOrderLine orderLine;
    private SalesOrderResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        client = Client.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .role(Role.CLIENT)
                .active(true)
                .build();

        warehouse = Warehouse.builder()
                .id(1L)
                .code("WH-001")
                .name("Warehouse Paris")
                .build();

        product = Product.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Laptop Dell XPS 15")
                .category("Electronics")
                .price(1299.99)
                .active(true)
                .build();

        SalesOrderCreateDto.OrderLineDto lineDto = SalesOrderCreateDto.OrderLineDto.builder()
                .sku("SKU-001")
                .quantity(2)
                .build();

        createDto = SalesOrderCreateDto.builder()
                .clientId(1L)
                .warehouseId(1L)
                .lines(Arrays.asList(lineDto))
                .build();

        orderLine = SalesOrderLine.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .price(1299.99)
                .build();

        salesOrder = SalesOrder.builder()
                .id(1L)
                .client(client)
                .warehouse(warehouse)
                .status(Status.CREATED)
                .createdAt(LocalDateTime.now())
                .lines(Arrays.asList(orderLine))
                .build();

        responseDto = SalesOrderResponseDto.builder()
                .id(1L)
                .clientId(1L)
                .warehouseId(1L)
                .status("CREATED")
                .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void testCreateOrder_Success() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        when(orderMapper.toEntity(createDto)).thenReturn(salesOrder);
        when(orderMapper.toLineEntity(any())).thenReturn(orderLine);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(orderMapper.toDto(salesOrder)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = orderService.createOrder(createDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getClientId());
        assertEquals(1L, result.getWarehouseId());
        assertEquals("CREATED", result.getStatus());

        verify(clientRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(productRepository, times(2)).findBySku("SKU-001"); // ✅ Corrigé : 2 fois
        verify(salesOrderRepository, times(1)).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when client not found")
    void testCreateOrder_ClientNotFound() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(createDto);
        });

        assertTrue(exception.getMessage().contains("Client non trouvé"));
        verify(clientRepository, times(1)).findById(1L);
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when warehouse not found")
    void testCreateOrder_WarehouseNotFound() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(createDto);
        });

        assertTrue(exception.getMessage().contains("Entrepôt source non trouvé"));
        verify(clientRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when product not found")
    void testCreateOrder_ProductNotFound() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(createDto);
        });

        assertTrue(exception.getMessage().contains("Produit inexistant ou inactif"));
        verify(productRepository, times(1)).findBySku("SKU-001");
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when product is inactive")
    void testCreateOrder_ProductInactive() {
        // Given
        product.setActive(false);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(createDto);
        });

        assertTrue(exception.getMessage().contains("Produit inexistant ou inactif"));
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void testGetOrderById_Success() {
        // Given
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(orderMapper.toDto(salesOrder)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = orderService.getOrderById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("CREATED", result.getStatus());

        verify(salesOrderRepository, times(1)).findById(1L);
        verify(orderMapper, times(1)).toDto(salesOrder);
    }

    @Test
    @DisplayName("Should throw RuntimeException when order not found by ID")
    void testGetOrderById_NotFound() {
        // Given
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(999L);
        });

        assertTrue(exception.getMessage().contains("Commande non trouvée"));
        verify(salesOrderRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create order with multiple lines")
    void testCreateOrder_MultipleLines() {
        // Given
        Product product2 = Product.builder()
                .id(2L)
                .sku("SKU-002")
                .name("iPhone 15")
                .category("Electronics")
                .price(999.99)
                .active(true)
                .build();

        SalesOrderCreateDto.OrderLineDto lineDto1 = SalesOrderCreateDto.OrderLineDto.builder()
                .sku("SKU-001")
                .quantity(2)
                .build();

        SalesOrderCreateDto.OrderLineDto lineDto2 = SalesOrderCreateDto.OrderLineDto.builder()
                .sku("SKU-002")
                .quantity(3)
                .build();

        createDto.setLines(Arrays.asList(lineDto1, lineDto2));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        when(productRepository.findBySku("SKU-002")).thenReturn(Optional.of(product2));
        when(orderMapper.toEntity(createDto)).thenReturn(salesOrder);
        when(orderMapper.toLineEntity(any())).thenReturn(orderLine);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(orderMapper.toDto(salesOrder)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = orderService.createOrder(createDto);

        // Then
        assertNotNull(result);
        verify(productRepository, times(2)).findBySku("SKU-001"); // ✅ Corrigé : 2 fois
        verify(productRepository, times(2)).findBySku("SKU-002"); // ✅ Corrigé : 2 fois
        verify(salesOrderRepository, times(1)).save(any(SalesOrder.class));
    }

    @ParameterizedTest
    @DisplayName("Should validate all products in order lines")
    @MethodSource("provideProductScenarios")
    void testCreateOrder_ValidateProducts(String sku, boolean active, boolean shouldFail) {
        // Given
        product.setSku(sku);
        product.setActive(active);

        SalesOrderCreateDto.OrderLineDto lineDto = SalesOrderCreateDto.OrderLineDto.builder()
                .sku(sku)
                .quantity(1)
                .build();
        createDto.setLines(Arrays.asList(lineDto));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findBySku(sku)).thenReturn(active ? Optional.of(product) : Optional.empty());

        // When & Then
        if (shouldFail) {
            assertThrows(RuntimeException.class, () -> {
                orderService.createOrder(createDto);
            });
        }
    }

    private static Stream<Arguments> provideProductScenarios() {
        return Stream.of(
                Arguments.of("SKU-001", false, true),
                Arguments.of("SKU-999", true, true)
        );
    }

    @Test
    @DisplayName("Should set correct order line properties")
    void testCreateOrder_OrderLineProperties() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        when(orderMapper.toEntity(createDto)).thenReturn(salesOrder);
        when(orderMapper.toLineEntity(any())).thenAnswer(invocation -> {
            SalesOrderLine line = new SalesOrderLine();
            line.setQuantity(2);
            return line;
        });
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder order = invocation.getArgument(0);
            assertNotNull(order.getLines());
            assertFalse(order.getLines().isEmpty());

            SalesOrderLine line = order.getLines().get(0);
            assertEquals(product, line.getProduct());
            assertEquals(product.getPrice(), line.getPrice());
            assertEquals(order, line.getSalesOrder());

            return order;
        });
        when(orderMapper.toDto(any(SalesOrder.class))).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = orderService.createOrder(createDto);

        // Then
        assertNotNull(result);
        verify(salesOrderRepository, times(1)).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Should handle order with empty lines")
    void testCreateOrder_EmptyLines() {
        // Given
        createDto.setLines(Arrays.asList());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(orderMapper.toEntity(createDto)).thenReturn(salesOrder);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(orderMapper.toDto(salesOrder)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = orderService.createOrder(createDto);

        // Then
        assertNotNull(result);
        verify(salesOrderRepository, times(1)).save(any(SalesOrder.class));
    }
}