package org.example.logistics.service;

import org.example.logistics.dto.inventory.InboundCreateDto;
import org.example.logistics.dto.inventory.InboundResponseDto;
import org.example.logistics.entity.Inventory;
import org.example.logistics.entity.InventoryMovement;
import org.example.logistics.entity.Product;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.entity.Enum.MovementType;
import org.example.logistics.mapper.InboundMapper;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("Inbound Service Tests")
class InboundServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InboundMapper inboundMapper;

    @InjectMocks
    private InboundService inboundService;

    private InboundCreateDto createDto;
    private Product product;
    private Warehouse warehouse;
    private Inventory inventory;
    private InventoryMovement movement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createDto = new InboundCreateDto();
        createDto.setProductId(1L);
        createDto.setWarehouseId(1L);
        createDto.setQuantity(50);
        createDto.setReferenceDoc("PO-2025-001");

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
                .type(MovementType.INBOUND)
                .quantity(50)
                .occurredAt(LocalDateTime.now())
                .referenceDoc("PO-2025-001")
                .build();
    }

    @Test
    @DisplayName("Should record inbound successfully when inventory exists")
    void testRecordInbound_InventoryExists_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            inv.setQtyOnHand(150); // 100 + 50
            return inv;
        });
        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When
        InboundResponseDto result = inboundService.recordInbound(createDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getProductId());
        assertEquals(1L, result.getWarehouseId());
        assertEquals(50, result.getQuantityAdded());
        assertEquals(150.0, result.getNewQtyOnHand());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("50"));

        verify(productRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(inventoryRepository, times(1)).findByProductIdAndWarehouseId(1L, 1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should create new inventory when it doesn't exist")
    void testRecordInbound_CreateNewInventory_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When
        InboundResponseDto result = inboundService.recordInbound(createDto);

        // Then
        assertNotNull(result);
        assertEquals(50.0, result.getNewQtyOnHand());
        assertEquals(50, result.getQuantityAdded());

        verify(inventoryRepository, times(2)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testRecordInbound_ProductNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inboundService.recordInbound(createDto);
        });

        assertTrue(exception.getMessage().contains("Produit non trouvé"));
        assertTrue(exception.getMessage().contains("ID 1"));

        verify(productRepository, times(1)).findById(1L);
        verify(warehouseRepository, never()).findById(anyLong());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should throw exception when warehouse not found")
    void testRecordInbound_WarehouseNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inboundService.recordInbound(createDto);
        });

        assertTrue(exception.getMessage().contains("Entrepôt non trouvé"));
        assertTrue(exception.getMessage().contains("ID 1"));

        verify(productRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for invalid quantities")
    @MethodSource("provideInvalidQuantities")
    void testRecordInbound_InvalidQuantity(Integer quantity) {
        // Given
        createDto.setQuantity(quantity);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inboundService.recordInbound(createDto);
        });

        assertTrue(exception.getMessage().contains("Quantité doit être positive"));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    private static Stream<Arguments> provideInvalidQuantities() {
        return Stream.of(
                Arguments.of(0),
                Arguments.of(-1),
                Arguments.of(-50)
        );
    }

    @Test
    @DisplayName("Should handle large quantity inbound")
    void testRecordInbound_LargeQuantity() {
        // Given
        createDto.setQuantity(10000);
        inventory.setQtyOnHand(100);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            inv.setQtyOnHand(10100); // 100 + 10000
            return inv;
        });
        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When
        InboundResponseDto result = inboundService.recordInbound(createDto);

        // Then
        assertNotNull(result);
        assertEquals(10000, result.getQuantityAdded());
        assertEquals(10100.0, result.getNewQtyOnHand());

        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should save inventory movement with correct details")
    void testRecordInbound_SavesMovementCorrectly() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When
        InboundResponseDto result = inboundService.recordInbound(createDto);

        // Then
        verify(inboundMapper, times(1)).toMovement(any(InboundCreateDto.class));
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
        assertNotNull(result.getOccurredAt());
    }

    @Test
    @DisplayName("Should handle inbound with reference document")
    void testRecordInbound_WithReferenceDoc() {
        // Given
        createDto.setReferenceDoc("PO-2025-999");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When
        InboundResponseDto result = inboundService.recordInbound(createDto);

        // Then
        assertNotNull(result);
        verify(inboundMapper, times(1)).toMovement(any(InboundCreateDto.class));
    }

    @Test
    @DisplayName("Should update qtyOnHand correctly")
    void testRecordInbound_UpdatesQtyOnHand() {
        // Given
        inventory.setQtyOnHand(100);
        createDto.setQuantity(50);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            assertEquals(150, inv.getQtyOnHand()); // Vérifie que qtyOnHand = 100 + 50
            return inv;
        });
        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When
        InboundResponseDto result = inboundService.recordInbound(createDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should handle multiple consecutive inbounds")
    void testRecordInbound_MultipleInbounds() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When - Premier inbound
        InboundResponseDto result1 = inboundService.recordInbound(createDto);

        // When - Deuxième inbound
        InboundResponseDto result2 = inboundService.recordInbound(createDto);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        verify(inventoryRepository, times(2)).save(any(Inventory.class));
        verify(inventoryMovementRepository, times(2)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should handle inbound for different products in same warehouse")
    void testRecordInbound_DifferentProducts() {
        // Given
        Product product2 = Product.builder()
                .id(2L)
                .sku("SKU-002")
                .name("iPhone 15")
                .category("Electronics")
                .active(true)
                .price(999.99)
                .build();

        createDto.setProductId(2L);

        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(2L, 1L))
                .thenReturn(Optional.empty());

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When
        InboundResponseDto result = inboundService.recordInbound(createDto);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getProductId());
        verify(productRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should return correct response message")
    void testRecordInbound_ResponseMessage() {
        // Given
        createDto.setQuantity(75);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When
        InboundResponseDto result = inboundService.recordInbound(createDto);

        // Then
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Réception enregistrée"));
        assertTrue(result.getMessage().contains("75"));
    }

    @Test
    @DisplayName("Should handle minimum quantity")
    void testRecordInbound_MinimumQuantity() {
        // Given
        createDto.setQuantity(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inboundMapper.toMovement(any(InboundCreateDto.class))).thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(movement);

        // When
        InboundResponseDto result = inboundService.recordInbound(createDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getQuantityAdded());
    }
}