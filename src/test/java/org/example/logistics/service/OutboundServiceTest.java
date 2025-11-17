package org.example.logistics.service;

import org.example.logistics.dto.inventory.OutboundCreateDto;
import org.example.logistics.dto.inventory.OutboundResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.InventoryMovement;
import org.example.logistics.entity.Product;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.entity.Enum.MovementType;
import org.example.logistics.mapper.OutboundMapper;
import org.example.logistics.repository.InventoryMovementRepository;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.ProductRepository;
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
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Outbound Service Tests")
class OutboundServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private OutboundMapper outboundMapper;

    @InjectMocks
    private OutboundService outboundService;

    private OutboundCreateDto createDto;
    private Product product;
    private Warehouse warehouse;
    private Inventory inventory;
    private InventoryMovement movement;
    private OutboundResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createDto = new OutboundCreateDto();
        createDto.setProductId(1L);
        createDto.setWarehouseId(1L);
        createDto.setQuantity(30);
        createDto.setReferenceDoc("SO-2025-001");

        product = Product.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Laptop Dell XPS 15")
                .category("Electronics")
                .active(true)
                .price(1299.99)
                .build();

        warehouse = Warehouse.builder()
                .id(1L)
                .code("WH-001")
                .name("Warehouse Paris")
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .warehouse(warehouse)
                .qtyOnHand(100)
                .qtyReserved(20)
                .build();

        movement = InventoryMovement.builder()
                .id(1L)
                .product(product)
                .warehouse(warehouse)
                .type(MovementType.OUTBOUND)
                .quantity(30)
                .occurredAt(LocalDateTime.now())
                .referenceDoc("SO-2025-001")
                .build();

        responseDto = new OutboundResponseDto();
        responseDto.setId(1L);
        responseDto.setType("OUTBOUND");
        responseDto.setQuantitySubtracted(30);
        responseDto.setNewQtyOnHand(70);
        responseDto.setOccurredAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should record outbound successfully")
    void testRecordOutbound_Success() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(outboundMapper.toMovement(createDto)).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
        when(outboundMapper.toDto(any(Inventory.class), any(OutboundCreateDto.class)))
                .thenReturn(responseDto);

        // When
        OutboundResponseDto result = outboundService.recordOutbound(createDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("OUTBOUND", result.getType());
        assertEquals(30, result.getQuantitySubtracted());
        assertNotNull(result.getMessage());

        verify(productRepository, times(1)).existsById(1L);
        verify(warehouseRepository, times(1)).existsById(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for various error scenarios")
    @MethodSource("provideErrorScenarios")
    void testRecordOutbound_ErrorScenarios(String errorMessage, boolean productExists,
                                           boolean warehouseExists, boolean inventoryExists) {
        // Given
        when(productRepository.existsById(1L)).thenReturn(productExists);
        when(warehouseRepository.existsById(1L)).thenReturn(warehouseExists);
        if (inventoryExists) {
            when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                    .thenReturn(Optional.of(inventory));
        } else {
            when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                    .thenReturn(Optional.empty());
        }

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            outboundService.recordOutbound(createDto);
        });

        assertTrue(exception.getMessage().contains(errorMessage));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    private static Stream<Arguments> provideErrorScenarios() {
        return Stream.of(
                Arguments.of("Produit non trouvé", false, true, true),
                Arguments.of("Entrepôt non trouvé", true, false, true),
                Arguments.of("Inventaire non trouvé", true, true, false)
        );
    }

    @Test
    @DisplayName("Should throw exception when quantity is zero")
    void testRecordOutbound_ZeroQuantity() {
        // Given
        createDto.setQuantity(0);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            outboundService.recordOutbound(createDto);
        });

        assertTrue(exception.getMessage().contains("Quantité doit être positive"));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should throw exception when quantity is negative")
    void testRecordOutbound_NegativeQuantity() {
        // Given
        createDto.setQuantity(-10);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            outboundService.recordOutbound(createDto);
        });

        assertTrue(exception.getMessage().contains("Quantité doit être positive"));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void testRecordOutbound_InsufficientStock() {
        // Given
        createDto.setQuantity(100); // Available = 80 (100 - 20)
        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            outboundService.recordOutbound(createDto);
        });

        assertTrue(exception.getMessage().contains("Stock insuffisant"));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should update qtyOnHand correctly")
    void testRecordOutbound_UpdateQtyOnHand() {
        // Given
        inventory.setQtyOnHand(100);
        createDto.setQuantity(30);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            assertEquals(70, inv.getQtyOnHand());
            return inv;
        });
        when(outboundMapper.toMovement(createDto)).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
        when(outboundMapper.toDto(any(Inventory.class), any(OutboundCreateDto.class)))
                .thenReturn(responseDto);

        // When
        OutboundResponseDto result = outboundService.recordOutbound(createDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should handle outbound that depletes stock")
    void testRecordOutbound_DepleteStock() {
        // Given
        inventory.setQtyOnHand(80);
        inventory.setQtyReserved(0);
        createDto.setQuantity(80);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            assertEquals(0, inv.getQtyOnHand());
            return inv;
        });
        when(outboundMapper.toMovement(createDto)).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
        when(outboundMapper.toDto(any(Inventory.class), any(OutboundCreateDto.class)))
                .thenReturn(responseDto);

        // When
        OutboundResponseDto result = outboundService.recordOutbound(createDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }
}