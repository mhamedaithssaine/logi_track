package org.example.logistics.service;

import org.example.logistics.dto.product.ProductCreateDto;
import org.example.logistics.dto.product.ProductResponseDto;
import org.example.logistics.dto.product.ProductUpdateDto;
import org.example.logistics.entity.Product;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.ProductMapper;
import org.example.logistics.repository.InventoryRepository;
import org.example.logistics.repository.ProductRepository;
import org.example.logistics.repository.SalesOrderLineRepository;
import org.example.logistics.repository.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductService productService;

    private ProductCreateDto createDto;
    private ProductUpdateDto updateDto;
    private Product product;
    private ProductResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createDto = ProductCreateDto.builder()
                .sku("SKU-001")
                .name("Laptop Dell XPS 15")
                .category("Electronics")
                .price(1299.99)
                .active(true)
                .build();

        updateDto = ProductUpdateDto.builder()
                .name("Laptop Dell XPS 15 Updated")
                .category("Electronics")
                .price(1399.99)
                .active(true)
                .build();

        product = Product.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Laptop Dell XPS 15")
                .category("Electronics")
                .price(1299.99)
                .active(true)
                .build();

        responseDto = ProductResponseDto.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Laptop Dell XPS 15")
                .category("Electronics")
                .price(1299.99)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        // Given
        when(productRepository.existsBySku(createDto.getSku())).thenReturn(false);
        when(productMapper.toEntity(createDto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponseDto result = productService.createProduct(createDto);

        // Then
        assertNotNull(result);
        assertEquals("SKU-001", result.getSku());
        assertEquals("Laptop Dell XPS 15", result.getName());
        assertEquals("Produit créé avec succès", result.getMessage());

        verify(productRepository, times(1)).existsBySku(createDto.getSku());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when SKU already exists")
    void testCreateProduct_SkuExists() {
        // Given
        when(productRepository.existsBySku(createDto.getSku())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.createProduct(createDto);
        });

        assertTrue(exception.getMessage().contains("SKU existe déjà"));
        verify(productRepository, times(1)).existsBySku(createDto.getSku());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should get all products successfully")
    void testGetAllProducts_Success() {
        // Given
        Product product2 = Product.builder()
                .id(2L)
                .sku("SKU-002")
                .name("iPhone 15")
                .category("Electronics")
                .price(999.99)
                .active(true)
                .build();

        List<Product> products = Arrays.asList(product, product2);
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toDto(any(Product.class))).thenReturn(responseDto);

        // When
        List<ProductResponseDto> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(productRepository, times(1)).findAll();
        verify(productMapper, times(2)).toDto(any(Product.class));
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void testGetAllProducts_EmptyList() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<ProductResponseDto> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get product by SKU successfully")
    void testGetBySku_Success() {
        // Given
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(responseDto);

        // When
        ProductResponseDto result = productService.getBySku("SKU-001");

        // Then
        assertNotNull(result);
        assertEquals("SKU-001", result.getSku());
        assertEquals("Laptop Dell XPS 15", result.getName());

        verify(productRepository, times(1)).findBySku("SKU-001");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found by SKU")
    void testGetBySku_NotFound() {
        // Given
        when(productRepository.findBySku("SKU-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getBySku("SKU-999");
        });

        verify(productRepository, times(1)).findBySku("SKU-999");
    }

    @Test
    @DisplayName("Should throw RuntimeException when product has null fields")
    void testGetBySku_NullFields() {
        // Given
        product.setName(null);
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getBySku("SKU-001");
        });

        assertTrue(exception.getMessage().contains("Produit incomplet"));
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() {
        // Given
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        doNothing().when(productMapper).toEntityFromUpdate(updateDto, product);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponseDto result = productService.updateProduct("SKU-001", updateDto);

        // Then
        assertNotNull(result);
        assertEquals("SKU-001", result.getSku());
        assertEquals("Produit mis à jour avec succès", result.getMessage());

        verify(productRepository, times(1)).findBySku("SKU-001");
        verify(productMapper, times(1)).toEntityFromUpdate(updateDto, product);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
    void testUpdateProduct_NotFound() {
        // Given
        when(productRepository.findBySku("SKU-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct("SKU-999", updateDto);
        });

        verify(productRepository, times(1)).findBySku("SKU-999");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_Success() {
        // Given
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        // When
        String result = productService.deleteProduct("SKU-001");

        // Then
        assertNotNull(result);
        assertEquals("Produit supprimé avec succès", result);

        verify(productRepository, times(1)).findBySku("SKU-001");
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
    void testDeleteProduct_NotFound() {
        // Given
        when(productRepository.findBySku("SKU-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct("SKU-999");
        });

        verify(productRepository, times(1)).findBySku("SKU-999");
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("Should deactivate product successfully")
    void testDeactivateProduct_Success() {
        // Given
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        when(salesOrderLineRepository.countByProductIdAndSalesOrderStatusIn(eq(1L), anyList())).thenReturn(0L);
        when(inventoryRepository.existsByProductIdAndQtyReservedGreaterThan(1L, 0)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponseDto result = productService.deactivateProduct("SKU-001");

        // Then
        assertNotNull(result);
        assertEquals("Produit désactivé avec succès", result.getMessage());
        assertFalse(product.getActive());

        verify(productRepository, times(1)).findBySku("SKU-001");
        verify(salesOrderLineRepository, times(1)).countByProductIdAndSalesOrderStatusIn(eq(1L), anyList());
        verify(inventoryRepository, times(1)).existsByProductIdAndQtyReservedGreaterThan(1L, 0);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Should throw RuntimeException when product has active orders")
    void testDeactivateProduct_HasActiveOrders() {
        // Given
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        when(salesOrderLineRepository.countByProductIdAndSalesOrderStatusIn(eq(1L), anyList())).thenReturn(5L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deactivateProduct("SKU-001");
        });

        assertTrue(exception.getMessage().contains("commandes actives"));
        verify(productRepository, times(1)).findBySku("SKU-001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when product has reserved stock")
    void testDeactivateProduct_HasReservedStock() {
        // Given
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        when(salesOrderLineRepository.countByProductIdAndSalesOrderStatusIn(eq(1L), anyList())).thenReturn(0L);
        when(inventoryRepository.existsByProductIdAndQtyReservedGreaterThan(1L, 0)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deactivateProduct("SKU-001");
        });

        assertTrue(exception.getMessage().contains("stock réserve"));
        verify(productRepository, times(1)).findBySku("SKU-001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deactivating non-existent product")
    void testDeactivateProduct_NotFound() {
        // Given
        when(productRepository.findBySku("SKU-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deactivateProduct("SKU-999");
        });

        verify(productRepository, times(1)).findBySku("SKU-999");
        verify(productRepository, never()).save(any(Product.class));
    }

    @ParameterizedTest
    @DisplayName("Should handle products with various null fields")
    @MethodSource("provideNullFieldScenarios")
    void testGetBySku_NullFieldsScenarios(String name, String category, Double price) {
        // Given
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));

        // When & Then
        if (name == null || category == null || price == null) {
            assertThrows(RuntimeException.class, () -> {
                productService.getBySku("SKU-001");
            });
        }
    }

    private static Stream<Arguments> provideNullFieldScenarios() {
        return Stream.of(
                Arguments.of(null, "Electronics", 1299.99),
                Arguments.of("Laptop", null, 1299.99),
                Arguments.of("Laptop", "Electronics", null)
        );
    }

    @Test
    @DisplayName("Should handle product with inactive status")
    void testGetBySku_InactiveProduct() {
        // Given
        product.setActive(false);
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(responseDto);

        // When
        ProductResponseDto result = productService.getBySku("SKU-001");

        // Then
        assertNotNull(result);
        verify(productRepository, times(1)).findBySku("SKU-001");
    }

    @Test
    @DisplayName("Should create product with all fields")
    void testCreateProduct_AllFields() {
        // Given
        when(productRepository.existsBySku(createDto.getSku())).thenReturn(false);
        when(productMapper.toEntity(createDto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponseDto result = productService.createProduct(createDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("SKU-001", result.getSku());
        assertEquals("Laptop Dell XPS 15", result.getName());
        assertEquals("Electronics", result.getCategory());
        assertEquals(1299.99, result.getPrice());
        assertTrue(result.isActive());
    }

    @Test
    @DisplayName("Should verify product availability check")
    void testProductAvailability() {
        // Given
        product.setActive(true);

        // When
        boolean available = product.isAvailable();

        // Then
        assertTrue(available);
    }

    @Test
    @DisplayName("Should verify product unavailable when inactive")
    void testProductUnavailable() {
        // Given
        product.setActive(false);

        // When
        boolean available = product.isAvailable();

        // Then
        assertFalse(available);
    }

    @Test
    @DisplayName("Should deactivate product and verify status changes")
    void testDeactivateProduct_VerifyStatusChange() {
        // Given
        assertTrue(product.getActive());
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));
        when(salesOrderLineRepository.countByProductIdAndSalesOrderStatusIn(eq(1L), anyList())).thenReturn(0L);
        when(inventoryRepository.existsByProductIdAndQtyReservedGreaterThan(1L, 0)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            assertFalse(p.getActive());
            return p;
        });

        // When
        ProductResponseDto result = productService.deactivateProduct("SKU-001");

        // Then
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }
}