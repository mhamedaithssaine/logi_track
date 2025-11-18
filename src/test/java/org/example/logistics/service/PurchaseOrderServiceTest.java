package org.example.logistics.service;

import org.example.logistics.dto.purchase.PurchaseOrderCreateDto;
import org.example.logistics.dto.purchase.PurchaseOrderReceiveDto;
import org.example.logistics.dto.purchase.PurchaseOrderResponseDto;
import org.example.logistics.entity.*;
import org.example.logistics.entity.Enum.MovementType;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.mapper.PurchaseOrderMapper;
import org.example.logistics.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("Purchase Order Service Tests")
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private PurchaseOrderMapper purchaseOrderMapper;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private PurchaseOrderCreateDto createDto;
    private PurchaseOrderReceiveDto receiveDto;
    private Supplier supplier;
    private Warehouse warehouse;
    private Product product;
    private PurchaseOrder purchaseOrder;
    private PurchaseOrderLine orderLine;
    private Inventory inventory;
    private PurchaseOrderResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        warehouse = Warehouse.builder()
                .id(1L)
                .code("WH-001")
                .name("Warehouse Paris")
                .build();

        supplier = Supplier.builder()
                .id(1L)
                .name("Tech Supplier Inc.")
                .contact("supplier@tech.com")
                .warehouse(warehouse)
                .build();

        product = Product.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Laptop Dell XPS 15")
                .category("Electronics")
                .active(true)
                .price(1299.99)
                .build();

        PurchaseOrderCreateDto.LineCreateDto lineCreateDto = PurchaseOrderCreateDto.LineCreateDto.builder()
                .productId(1L)
                .quantity(100)
                .build();

        createDto = PurchaseOrderCreateDto.builder()
                .supplierId(1L)
                .lines(Arrays.asList(lineCreateDto))
                .build();

        orderLine = PurchaseOrderLine.builder()
                .id(1L)
                .product(product)
                .quantity(100)
                .receivedQty(0)
                .build();

        purchaseOrder = PurchaseOrder.builder()
                .id(1L)
                .supplier(supplier)
                .status(Status.APPROVED)
                .lines(Arrays.asList(orderLine))
                .build();

        orderLine.setPurchaseOrder(purchaseOrder);

        inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .warehouse(warehouse)
                .qtyOnHand(50)
                .qtyReserved(10)
                .build();

        PurchaseOrderReceiveDto.LineReceiveDto lineReceiveDto = PurchaseOrderReceiveDto.LineReceiveDto.builder()
                .receivedQuantity(100)
                .build();

        receiveDto = PurchaseOrderReceiveDto.builder()
                .lines(Arrays.asList(lineReceiveDto))
                .build();

        responseDto = PurchaseOrderResponseDto.builder()
                .id(1L)
                .status("APPROVED")
                .build();
    }

    @Test
    @DisplayName("Should create purchase order successfully")
    void testCreatePurchaseOrder_Success() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.createPurchaseOrder(createDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("APPROVED", result.getStatus());

        verify(supplierRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should throw exception when supplier not found")
    void testCreatePurchaseOrder_SupplierNotFound() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            purchaseOrderService.createPurchaseOrder(createDto);
        });

        assertTrue(exception.getMessage().contains("Fournisseur non trouvé"));
        verify(supplierRepository, times(1)).findById(1L);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found during PO creation")
    void testCreatePurchaseOrder_ProductNotFound() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            purchaseOrderService.createPurchaseOrder(createDto);
        });

        assertTrue(exception.getMessage().contains("Produit non trouvé"));
        verify(productRepository, times(1)).findById(1L);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should create PO with multiple lines")
    void testCreatePurchaseOrder_MultipleLines() {
        // Given
        Product product2 = Product.builder()
                .id(2L)
                .sku("SKU-002")
                .name("iPhone 15")
                .category("Electronics")
                .active(true)
                .price(999.99)
                .build();

        PurchaseOrderCreateDto.LineCreateDto lineCreateDto1 = PurchaseOrderCreateDto.LineCreateDto.builder()
                .productId(1L)
                .quantity(100)
                .build();

        PurchaseOrderCreateDto.LineCreateDto lineCreateDto2 = PurchaseOrderCreateDto.LineCreateDto.builder()
                .productId(2L)
                .quantity(50)
                .build();

        createDto.setLines(Arrays.asList(lineCreateDto1, lineCreateDto2));

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.createPurchaseOrder(createDto);

        // Then
        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(2L);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should create PO with correct initial status")
    void testCreatePurchaseOrder_InitialStatus() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            assertEquals(Status.APPROVED, po.getStatus());
            assertEquals(supplier, po.getSupplier());
            assertNotNull(po.getLines());
            assertEquals(1, po.getLines().size());
            return po;
        });
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.createPurchaseOrder(createDto);

        // Then
        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should receive purchase order successfully")
    void testReceivePurchaseOrder_Success() {
        // Given
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderLineRepository.save(any(PurchaseOrderLine.class))).thenReturn(orderLine);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenReturn(new InventoryMovement());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.receivePurchaseOrder(1L, receiveDto);

        // Then
        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).findByIdAndStatus(1L, Status.APPROVED);
        verify(purchaseOrderLineRepository, times(1)).save(any(PurchaseOrderLine.class));
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should throw exception when PO not found or not approved")
    void testReceivePurchaseOrder_PONotFoundOrNotApproved() {
        // Given
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            purchaseOrderService.receivePurchaseOrder(1L, receiveDto);
        });

        assertTrue(exception.getMessage().contains("PO non trouvé ou non approuvé"));
        verify(purchaseOrderLineRepository, never()).save(any(PurchaseOrderLine.class));
    }

    @Test
    @DisplayName("Should throw exception when supplier has no warehouse")
    void testReceivePurchaseOrder_SupplierNoWarehouse() {
        // Given
        supplier.setWarehouse(null);
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.of(purchaseOrder));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            purchaseOrderService.receivePurchaseOrder(1L, receiveDto);
        });

        // ✅ CORRIGÉ : Vérifier plusieurs variantes possibles du message
        String message = exception.getMessage().toLowerCase();
        assertTrue(
                message.contains("warehouse") &&
                        (message.contains("associé") || message.contains("pas de")),
                "Expected message about missing warehouse but got: " + exception.getMessage()
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should throw exception when inventory not found during receive")
    void testReceivePurchaseOrder_InventoryNotFound() {
        // Given
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.of(purchaseOrder));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            purchaseOrderService.receivePurchaseOrder(1L, receiveDto);
        });

        assertTrue(exception.getMessage().contains("Inventaire non trouvé"));
        verify(inventoryMovementRepository, never()).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should update inventory quantity correctly")
    void testReceivePurchaseOrder_UpdateInventoryQuantity() {
        // Given
        inventory.setQtyOnHand(50);
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderLineRepository.save(any(PurchaseOrderLine.class))).thenReturn(orderLine);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            assertEquals(150, inv.getQtyOnHand()); // 50 + 100
            return inv;
        });
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenReturn(new InventoryMovement());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.receivePurchaseOrder(1L, receiveDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should update PO status to RECEIVED")
    void testReceivePurchaseOrder_UpdateStatus() {
        // Given
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderLineRepository.save(any(PurchaseOrderLine.class))).thenReturn(orderLine);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenReturn(new InventoryMovement());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            assertEquals(Status.RECEIVED, po.getStatus());
            return po;
        });
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.receivePurchaseOrder(1L, receiveDto);

        // Then
        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should create inventory movement with correct details")
    void testReceivePurchaseOrder_CreateMovement() {
        // Given
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderLineRepository.save(any(PurchaseOrderLine.class))).thenReturn(orderLine);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(invocation -> {
                    InventoryMovement movement = invocation.getArgument(0);
                    assertEquals(MovementType.INBOUND, movement.getType());
                    assertEquals(100, movement.getQuantity());
                    assertEquals(product, movement.getProduct());
                    assertEquals(warehouse, movement.getWarehouse());
                    assertTrue(movement.getReferenceDoc().contains("PO"));
                    return movement;
                });
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.receivePurchaseOrder(1L, receiveDto);

        // Then
        assertNotNull(result);
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should not create movement when received quantity is zero")
    void testReceivePurchaseOrder_ZeroQuantity() {
        // Given
        receiveDto.getLines().get(0).setReceivedQuantity(0);
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderLineRepository.save(any(PurchaseOrderLine.class))).thenReturn(orderLine);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.receivePurchaseOrder(1L, receiveDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(inventoryMovementRepository, never()).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should update received quantity on order line")
    void testReceivePurchaseOrder_UpdateReceivedQty() {
        // Given
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderLineRepository.save(any(PurchaseOrderLine.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderLine line = invocation.getArgument(0);
                    assertEquals(100, line.getReceivedQty());
                    return line;
                });
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenReturn(new InventoryMovement());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.receivePurchaseOrder(1L, receiveDto);

        // Then
        assertNotNull(result);
        verify(purchaseOrderLineRepository, times(1)).save(any(PurchaseOrderLine.class));
    }

    @Test
    @DisplayName("Should handle partial receipt")
    void testReceivePurchaseOrder_PartialReceipt() {
        // Given
        receiveDto.getLines().get(0).setReceivedQuantity(50);
        when(purchaseOrderRepository.findByIdAndStatus(1L, Status.APPROVED))
                .thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderLineRepository.save(any(PurchaseOrderLine.class))).thenReturn(orderLine);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            assertEquals(100, inv.getQtyOnHand()); // 50 + 50
            return inv;
        });
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenReturn(new InventoryMovement());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.receivePurchaseOrder(1L, receiveDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should handle empty lines list during creation")
    void testCreatePurchaseOrder_EmptyLines() {
        // Given
        createDto.setLines(Collections.emptyList());
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.createPurchaseOrder(createDto);

        // Then
        assertNotNull(result);
        verify(productRepository, never()).findById(anyLong());
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should set correct line properties during creation")
    void testCreatePurchaseOrder_LineProperties() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            assertEquals(1, po.getLines().size());
            PurchaseOrderLine line = po.getLines().get(0);
            assertEquals(product, line.getProduct());
            assertEquals(100, line.getQuantity());
            assertEquals(0, line.getReceivedQty());
            assertEquals(po, line.getPurchaseOrder());
            return po;
        });
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.createPurchaseOrder(createDto);

        // Then
        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }
}