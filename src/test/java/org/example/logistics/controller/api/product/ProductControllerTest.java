package org.example.logistics.controller.api.product;

import org.example.logistics.dto.product.ProductCreateDto;
import org.example.logistics.dto.product.ProductResponseDto;
import org.example.logistics.dto.product.ProductUpdateDto;
import org.example.logistics.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductCreateDto createDto;
    private ProductUpdateDto updateDto;
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

        responseDto = ProductResponseDto.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Laptop Dell XPS 15")
                .category("Electronics")
                .price(1299.99)
                .active(true)
                .message("Opération réussie")
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        // Given
        when(productService.createProduct(any(ProductCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<ProductResponseDto> response = productController.createProduct(createDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("SKU-001", response.getBody().getSku());
        assertEquals("Laptop Dell XPS 15", response.getBody().getName());
        assertEquals("Electronics", response.getBody().getCategory());
        assertEquals(1299.99, response.getBody().getPrice());
        assertTrue(response.getBody().isActive());

        verify(productService, times(1)).createProduct(any(ProductCreateDto.class));
    }

    @Test
    @DisplayName("Should get all products successfully")
    void testGetAllProducts_Success() {
        // Given
        ProductResponseDto product2 = ProductResponseDto.builder()
                .id(2L)
                .sku("SKU-002")
                .name("iPhone 15")
                .category("Electronics")
                .price(999.99)
                .active(true)
                .build();

        List<ProductResponseDto> products = Arrays.asList(responseDto, product2);
        when(productService.getAllProducts()).thenReturn(products);

        // When
        ResponseEntity<List<ProductResponseDto>> response = productController.getAllProducts();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("SKU-001", response.getBody().get(0).getSku());
        assertEquals("SKU-002", response.getBody().get(1).getSku());

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void testGetAllProducts_EmptyList() {
        // Given
        when(productService.getAllProducts()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<ProductResponseDto>> response = productController.getAllProducts();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("Should get product by SKU successfully")
    void testGetProductBySku_Success() {
        // Given
        when(productService.getBySku("SKU-001")).thenReturn(responseDto);

        // When
        ResponseEntity<ProductResponseDto> response = productController.getProductBySku("SKU-001");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SKU-001", response.getBody().getSku());
        assertEquals("Laptop Dell XPS 15", response.getBody().getName());

        verify(productService, times(1)).getBySku("SKU-001");
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() {
        // Given
        ProductResponseDto updatedResponse = ProductResponseDto.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Laptop Dell XPS 15 Updated")
                .category("Electronics")
                .price(1399.99)
                .active(true)
                .message("Produit mis à jour avec succès")
                .build();

        when(productService.updateProduct(eq("SKU-001"), any(ProductUpdateDto.class)))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<ProductResponseDto> response = productController.updateProduct("SKU-001", updateDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Laptop Dell XPS 15 Updated", response.getBody().getName());
        assertEquals(1399.99, response.getBody().getPrice());

        verify(productService, times(1)).updateProduct(eq("SKU-001"), any(ProductUpdateDto.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_Success() {
        // Given
        when(productService.deleteProduct("SKU-001"))
                .thenReturn("Produit supprimé avec succès");

        // When
        ResponseEntity<String> response = productController.deleteProduct("SKU-001");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Produit supprimé avec succès", response.getBody());

        verify(productService, times(1)).deleteProduct("SKU-001");
    }

    @Test
    @DisplayName("Should deactivate product successfully")
    void testDeactivateProduct_Success() {
        // Given
        ProductResponseDto deactivatedResponse = ProductResponseDto.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Laptop Dell XPS 15")
                .category("Electronics")
                .price(1299.99)
                .active(false)
                .message("Produit désactivé avec succès")
                .build();

        when(productService.deactivateProduct("SKU-001")).thenReturn(deactivatedResponse);

        // When
        ResponseEntity<ProductResponseDto> response = productController.deactivateProduct("SKU-001");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SKU-001", response.getBody().getSku());
        assertFalse(response.getBody().isActive());

        verify(productService, times(1)).deactivateProduct("SKU-001");
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for various create product scenarios")
    @MethodSource("provideCreateErrorScenarios")
    void testCreateProduct_ErrorScenarios(String errorMessage) {
        // Given
        when(productService.createProduct(any(ProductCreateDto.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productController.createProduct(createDto);
        });

        verify(productService, times(1)).createProduct(any(ProductCreateDto.class));
    }

    private static Stream<Arguments> provideCreateErrorScenarios() {
        return Stream.of(
                Arguments.of("SKU déjà existant"),
                Arguments.of("Données invalides")
        );
    }

    @ParameterizedTest
    @DisplayName("Should throw exception when product not found by SKU")
    @MethodSource("provideNotFoundScenarios")
    void testGetProductBySku_NotFound(String sku, String errorMessage) {
        // Given
        when(productService.getBySku(sku))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productController.getProductBySku(sku);
        });

        verify(productService, times(1)).getBySku(sku);
    }

    private static Stream<Arguments> provideNotFoundScenarios() {
        return Stream.of(
                Arguments.of("SKU-999", "Produit introuvable"),
                Arguments.of("INVALID", "SKU invalide")
        );
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for various update scenarios")
    @MethodSource("provideUpdateErrorScenarios")
    void testUpdateProduct_ErrorScenarios(String sku, String errorMessage) {
        // Given
        when(productService.updateProduct(eq(sku), any(ProductUpdateDto.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productController.updateProduct(sku, updateDto);
        });

        verify(productService, times(1)).updateProduct(eq(sku), any(ProductUpdateDto.class));
    }

    private static Stream<Arguments> provideUpdateErrorScenarios() {
        return Stream.of(
                Arguments.of("SKU-999", "Produit introuvable"),
                Arguments.of("SKU-001", "Mise à jour invalide")
        );
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for various delete scenarios")
    @MethodSource("provideDeleteErrorScenarios")
    void testDeleteProduct_ErrorScenarios(String sku, String errorMessage) {
        // Given
        when(productService.deleteProduct(sku))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productController.deleteProduct(sku);
        });

        verify(productService, times(1)).deleteProduct(sku);
    }

    private static Stream<Arguments> provideDeleteErrorScenarios() {
        return Stream.of(
                Arguments.of("SKU-999", "Produit introuvable"),
                Arguments.of("SKU-001", "Impossible de supprimer ce produit")
        );
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for various deactivate scenarios")
    @MethodSource("provideDeactivateErrorScenarios")
    void testDeactivateProduct_ErrorScenarios(String sku, String errorMessage) {
        // Given
        when(productService.deactivateProduct(sku))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productController.deactivateProduct(sku);
        });

        verify(productService, times(1)).deactivateProduct(sku);
    }

    private static Stream<Arguments> provideDeactivateErrorScenarios() {
        return Stream.of(
                Arguments.of("SKU-999", "Produit introuvable"),
                Arguments.of("SKU-001", "Produit déjà inactif")
        );
    }

    @Test
    @DisplayName("Should create product with minimum price")
    void testCreateProduct_MinimalPrice() {
        // Given
        ProductCreateDto minimalDto = ProductCreateDto.builder()
                .sku("SKU-002")
                .name("Basic Product")
                .category("General")
                .price(0.0)
                .active(true)
                .build();

        ProductResponseDto minimalResponse = ProductResponseDto.builder()
                .id(2L)
                .sku("SKU-002")
                .name("Basic Product")
                .category("General")
                .price(0.0)
                .active(true)
                .build();

        when(productService.createProduct(any(ProductCreateDto.class))).thenReturn(minimalResponse);

        // When
        ResponseEntity<ProductResponseDto> response = productController.createProduct(minimalDto);

        // Then
        assertNotNull(response);
        assertEquals(0.0, response.getBody().getPrice());

        verify(productService, times(1)).createProduct(any(ProductCreateDto.class));
    }

    @Test
    @DisplayName("Should handle special characters in SKU")
    void testGetProductBySku_SpecialCharacters() {
        // Given
        String specialSku = "SKU-001-A/B";
        ProductResponseDto specialResponse = ProductResponseDto.builder()
                .id(1L)
                .sku(specialSku)
                .name("Special Product")
                .category("Electronics")
                .price(1299.99)
                .active(true)
                .build();

        when(productService.getBySku(specialSku)).thenReturn(specialResponse);

        // When
        ResponseEntity<ProductResponseDto> response = productController.getProductBySku(specialSku);

        // Then
        assertNotNull(response);
        assertEquals(specialSku, response.getBody().getSku());

        verify(productService, times(1)).getBySku(specialSku);
    }

    @Test
    @DisplayName("Should handle products with different categories")
    void testCreateProduct_DifferentCategories() {
        // Given
        ProductCreateDto furnitureDto = ProductCreateDto.builder()
                .sku("SKU-003")
                .name("Office Chair")
                .category("Furniture")
                .price(299.99)
                .active(true)
                .build();

        ProductResponseDto furnitureResponse = ProductResponseDto.builder()
                .id(3L)
                .sku("SKU-003")
                .name("Office Chair")
                .category("Furniture")
                .price(299.99)
                .active(true)
                .build();

        when(productService.createProduct(any(ProductCreateDto.class))).thenReturn(furnitureResponse);

        // When
        ResponseEntity<ProductResponseDto> response = productController.createProduct(furnitureDto);

        // Then
        assertNotNull(response);
        assertEquals("Furniture", response.getBody().getCategory());

        verify(productService, times(1)).createProduct(any(ProductCreateDto.class));
    }

    @Test
    @DisplayName("Should handle high-priced products")
    void testCreateProduct_HighPrice() {
        // Given
        ProductCreateDto expensiveDto = ProductCreateDto.builder()
                .sku("SKU-004")
                .name("Luxury Item")
                .category("Premium")
                .price(99999.99)
                .active(true)
                .build();

        ProductResponseDto expensiveResponse = ProductResponseDto.builder()
                .id(4L)
                .sku("SKU-004")
                .name("Luxury Item")
                .category("Premium")
                .price(99999.99)
                .active(true)
                .build();

        when(productService.createProduct(any(ProductCreateDto.class))).thenReturn(expensiveResponse);

        // When
        ResponseEntity<ProductResponseDto> response = productController.createProduct(expensiveDto);

        // Then
        assertNotNull(response);
        assertEquals(99999.99, response.getBody().getPrice());

        verify(productService, times(1)).createProduct(any(ProductCreateDto.class));
    }

    @Test
    @DisplayName("Should update only specific fields")
    void testUpdateProduct_PartialUpdate() {
        // Given
        ProductUpdateDto partialUpdateDto = ProductUpdateDto.builder()
                .name("Updated Name Only")
                .category("Electronics")
                .price(1299.99)
                .active(true)
                .build();

        ProductResponseDto partialResponse = ProductResponseDto.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Updated Name Only")
                .category("Electronics")
                .price(1299.99)
                .active(true)
                .build();

        when(productService.updateProduct(eq("SKU-001"), any(ProductUpdateDto.class)))
                .thenReturn(partialResponse);

        // When
        ResponseEntity<ProductResponseDto> response = productController.updateProduct("SKU-001", partialUpdateDto);

        // Then
        assertNotNull(response);
        assertEquals("Updated Name Only", response.getBody().getName());

        verify(productService, times(1)).updateProduct(eq("SKU-001"), any(ProductUpdateDto.class));
    }

    @Test
    @DisplayName("Should handle deactivating already inactive product")
    void testDeactivateProduct_AlreadyInactive() {
        // Given
        ProductResponseDto inactiveResponse = ProductResponseDto.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Laptop Dell XPS 15")
                .category("Electronics")
                .price(1299.99)
                .active(false)
                .message("Produit déjà inactif")
                .build();

        when(productService.deactivateProduct("SKU-001")).thenReturn(inactiveResponse);

        // When
        ResponseEntity<ProductResponseDto> response = productController.deactivateProduct("SKU-001");

        // Then
        assertNotNull(response);
        assertFalse(response.getBody().isActive());

        verify(productService, times(1)).deactivateProduct("SKU-001");
    }

    @Test
    @DisplayName("Should create inactive product")
    void testCreateProduct_Inactive() {
        // Given
        ProductCreateDto inactiveDto = ProductCreateDto.builder()
                .sku("SKU-005")
                .name("Discontinued Product")
                .category("Electronics")
                .price(100.0)
                .active(false)
                .build();

        ProductResponseDto inactiveResponse = ProductResponseDto.builder()
                .id(5L)
                .sku("SKU-005")
                .name("Discontinued Product")
                .category("Electronics")
                .price(100.0)
                .active(false)
                .build();

        when(productService.createProduct(any(ProductCreateDto.class))).thenReturn(inactiveResponse);

        // When
        ResponseEntity<ProductResponseDto> response = productController.createProduct(inactiveDto);

        // Then
        assertNotNull(response);
        assertFalse(response.getBody().isActive());

        verify(productService, times(1)).createProduct(any(ProductCreateDto.class));
    }
}