package org.example.logistics.service;

import org.example.logistics.dto.inventory.AdjustmentCreateDto;
import org.example.logistics.dto.inventory.AdjustmentResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.InventoryMovement;
import org.example.logistics.entity.Product;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.entity.Enum.MovementType;
import org.example.logistics.mapper.AdjustmentMapper;
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

@DisplayName("Adjustment Service Tests")
class AdjustmentServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private AdjustmentMapper adjustmentMapper;

    @InjectMocks
    private AdjustmentService adjustmentService;

    private AdjustmentCreateDto createDto;
    private Product product;
    private Warehouse warehouse;
    private Inventory inventory;
    private InventoryMovement movement;
    private AdjustmentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createDto = new AdjustmentCreateDto();
        createDto.setProductId(1L);
        createDto.setWarehouseId(1L);
        createDto.setAdjustmentQty(10);
        createDto.setReason("Correction d'inventaire");

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
                .type(MovementType.ADJUSTMENT)
                .quantity(10)
                .occurredAt(LocalDateTime.now())
                .referenceDoc("ADJ-001")
                .build();

        responseDto = new AdjustmentResponseDto();
        responseDto.setId(1L);
        responseDto.setType("ADJUSTMENT");
        responseDto.setAdjustmentApplied(10);
        responseDto.setNewQtyOnHand(110);
        responseDto.setOccurredAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should adjust stock with positive quantity successfully")
    void testAdjustStock_PositiveQuantity_Success() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(adjustmentMapper.toMovement(createDto)).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
        when(adjustmentMapper.toDto(any(Inventory.class), any(AdjustmentCreateDto.class)))
                .thenReturn(responseDto);

        // When
        AdjustmentResponseDto result = adjustmentService.adjustStock(createDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ADJUSTMENT", result.getType());
        assertEquals(10, result.getAdjustmentApplied());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("10"));

        verify(productRepository, times(1)).existsById(1L);
        verify(warehouseRepository, times(1)).existsById(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should adjust stock with negative quantity successfully")
    void testAdjustStock_NegativeQuantity_Success() {
        // Given
        createDto.setAdjustmentQty(-10);
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(20);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(adjustmentMapper.toMovement(createDto)).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
        when(adjustmentMapper.toDto(any(Inventory.class), any(AdjustmentCreateDto.class)))
                .thenReturn(responseDto);

        // When
        AdjustmentResponseDto result = adjustmentService.adjustStock(createDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testAdjustStock_ProductNotFound() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adjustmentService.adjustStock(createDto);
        });

        assertTrue(exception.getMessage().contains("Produit non trouvé"));
        verify(productRepository, times(1)).existsById(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should throw exception when warehouse not found")
    void testAdjustStock_WarehouseNotFound() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adjustmentService.adjustStock(createDto);
        });

        assertTrue(exception.getMessage().contains("Entrepôt non trouvé"));
        verify(warehouseRepository, times(1)).existsById(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should throw exception when inventory not found")
    void testAdjustStock_InventoryNotFound() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adjustmentService.adjustStock(createDto);
        });

        assertTrue(exception.getMessage().contains("Inventaire non trouvé"));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should throw exception when negative adjustment exceeds available stock")
    void testAdjustStock_NegativeExceedsAvailable() {
        // Given
        createDto.setAdjustmentQty(-90);
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(20); // Available = 80, need 90

        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adjustmentService.adjustStock(createDto);
        });

        assertTrue(exception.getMessage().contains("Stock insuffisant"));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should handle zero adjustment")
    void testAdjustStock_ZeroQuantity() {
        // Given
        createDto.setAdjustmentQty(0);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(adjustmentMapper.toMovement(createDto)).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
        when(adjustmentMapper.toDto(any(Inventory.class), any(AdjustmentCreateDto.class)))
                .thenReturn(responseDto);

        // When
        AdjustmentResponseDto result = adjustmentService.adjustStock(createDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should update qtyOnHand correctly with positive adjustment")
    void testAdjustStock_UpdateQtyOnHand_Positive() {
        // Given
        inventory.setQtyOnHand(100);
        createDto.setAdjustmentQty(25);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            assertEquals(125, inv.getQtyOnHand());
            return inv;
        });
        when(adjustmentMapper.toMovement(createDto)).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
        when(adjustmentMapper.toDto(any(Inventory.class), any(AdjustmentCreateDto.class)))
                .thenReturn(responseDto);

        // When
        AdjustmentResponseDto result = adjustmentService.adjustStock(createDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should update qtyOnHand correctly with negative adjustment")
    void testAdjustStock_UpdateQtyOnHand_Negative() {
        // Given
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(10);
        createDto.setAdjustmentQty(-30);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            assertEquals(70, inv.getQtyOnHand());
            return inv;
        });
        when(adjustmentMapper.toMovement(createDto)).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
        when(adjustmentMapper.toDto(any(Inventory.class), any(AdjustmentCreateDto.class)))
                .thenReturn(responseDto);

        // When
        AdjustmentResponseDto result = adjustmentService.adjustStock(createDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @ParameterizedTest
    @DisplayName("Should handle various negative adjustment scenarios")
    @MethodSource("provideNegativeAdjustmentScenarios")
    void testAdjustStock_NegativeScenarios(int qtyOnHand, int qtyReserved, int adjustment, boolean shouldFail) {
        // Given
        inventory.setQtyOnHand(qtyOnHand);
        inventory.setQtyReserved(qtyReserved);
        createDto.setAdjustmentQty(adjustment);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        if (!shouldFail) {
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
            when(adjustmentMapper.toMovement(createDto)).thenReturn(movement);
            when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
            when(adjustmentMapper.toDto(any(Inventory.class), any(AdjustmentCreateDto.class)))
                    .thenReturn(responseDto);
        }

        // When & Then
        if (shouldFail) {
            assertThrows(RuntimeException.class, () -> {
                adjustmentService.adjustStock(createDto);
            });
        } else {
            AdjustmentResponseDto result = adjustmentService.adjustStock(createDto);
            assertNotNull(result);
        }
    }

    private static Stream<Arguments> provideNegativeAdjustmentScenarios() {
        return Stream.of(
                Arguments.of(100, 20, -79, false),  // OK: 100 >= 20 + 79
                Arguments.of(100, 20, -80, false),  // OK: 100 >= 20 + 80
                Arguments.of(100, 20, -81, true),   // FAIL: 100 < 20 + 81
                Arguments.of(50, 30, -20, false),   // OK: 50 >= 30 + 20
                Arguments.of(50, 30, -21, true)     // FAIL: 50 < 30 + 21
        );
    }

    @Test
    @DisplayName("Should save inventory movement with correct details")
    void testAdjustStock_SavesMovementCorrectly() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(adjustmentMapper.toMovement(createDto)).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);
        when(adjustmentMapper.toDto(any(Inventory.class), any(AdjustmentCreateDto.class)))
                .thenReturn(responseDto);

        // When
        AdjustmentResponseDto result = adjustmentService.adjustStock(createDto);

        // Then
        verify(adjustmentMapper, times(1)).toMovement(createDto);
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
        assertNotNull(result.getOccurredAt());
    }
}