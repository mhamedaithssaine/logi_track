package org.example.logistics.service;

import org.example.logistics.dto.supplier.SupplierCreateDto;
import org.example.logistics.dto.supplier.SupplierResponseDto;
import org.example.logistics.dto.supplier.SupplierUpdateDto;
import org.example.logistics.entity.Supplier;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.mapper.SupplierMapper;
import org.example.logistics.repository.SupplierRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;
    private Warehouse warehouse;
    private SupplierCreateDto createDto;
    private SupplierUpdateDto updateDto;
    private SupplierResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Setup Warehouse
        warehouse = Warehouse.builder()
                .id(1L)
                .code("WH001")
                .name("Entrepôt Principal")
                .build();

        // Setup Supplier
        supplier = Supplier.builder()
                .id(1L)
                .name("Fournisseur Test")
                .contact("contact@test.com")
                .warehouse(warehouse)
                .build();

        // Setup CreateDto
        createDto = SupplierCreateDto.builder()
                .name("Fournisseur Test")
                .contact("contact@test.com")
                .warehouseId(1L)
                .build();

        // Setup UpdateDto
        updateDto = SupplierUpdateDto.builder()
                .name("Fournisseur Modifié")
                .contact("nouveau@test.com")
                .warehouseId(1L)
                .build();

        // Setup ResponseDto
        responseDto = SupplierResponseDto.builder()
                .id(1L)
                .name("Fournisseur Test")
                .contact("contact@test.com")
                .warehouseId(1L)
                .warehouseName("Entrepôt Principal")
                .build();
    }

    // ==================== CREATE TESTS ====================

    @Test
    void testCreateSupplier_Success() {
        // Arrange
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(supplierMapper.toEntity(createDto)).thenReturn(supplier);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        // Act
        SupplierResponseDto result = supplierService.createSupplier(createDto);

        // Assert
        assertNotNull(result);
        assertEquals("Fournisseur Test", result.getName());
        assertEquals("contact@test.com", result.getContact());
        assertEquals(1L, result.getWarehouseId());
        assertEquals("Entrepôt Principal", result.getWarehouseName());
        assertEquals("Fournisseur créé avec succès", result.getMessage());

        verify(warehouseRepository, times(1)).existsById(1L);
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void testCreateSupplier_WithoutWarehouse_Success() {
        // Arrange
        createDto.setWarehouseId(null);
        supplier.setWarehouse(null);

        when(supplierMapper.toEntity(createDto)).thenReturn(supplier);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        // Act
        SupplierResponseDto result = supplierService.createSupplier(createDto);

        // Assert
        assertNotNull(result);
        assertEquals("Fournisseur Test", result.getName());
        assertNull(result.getWarehouseId());
        assertNull(result.getWarehouseName());
        assertEquals("Fournisseur créé avec succès", result.getMessage());

        verify(warehouseRepository, never()).existsById(any());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void testCreateSupplier_WarehouseNotFound_ThrowsException() {
        // Arrange
        when(warehouseRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            supplierService.createSupplier(createDto);
        });

        assertEquals("Entrepôt non trouvé : ID 1", exception.getMessage());
        verify(supplierRepository, never()).save(any());
    }

    // ==================== READ ALL TESTS ====================

    @Test
    void testGetAllSuppliers_Success() {
        // Arrange
        Supplier supplier2 = Supplier.builder()
                .id(2L)
                .name("Fournisseur 2")
                .contact("contact2@test.com")
                .build();

        List<Supplier> suppliers = Arrays.asList(supplier, supplier2);

        SupplierResponseDto responseDto2 = SupplierResponseDto.builder()
                .id(2L)
                .name("Fournisseur 2")
                .contact("contact2@test.com")
                .build();

        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierMapper.toDto(supplier)).thenReturn(responseDto);
        when(supplierMapper.toDto(supplier2)).thenReturn(responseDto2);

        // Act
        List<SupplierResponseDto> result = supplierService.getAllSuppliers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Fournisseur Test", result.get(0).getName());
        assertEquals("Fournisseur 2", result.get(1).getName());

        verify(supplierRepository, times(1)).findAll();
        verify(supplierMapper, times(2)).toDto(any(Supplier.class));
    }

    @Test
    void testGetAllSuppliers_EmptyList() {
        // Arrange
        when(supplierRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<SupplierResponseDto> result = supplierService.getAllSuppliers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository, times(1)).findAll();
    }

    // ==================== READ BY ID TESTS ====================

    @Test
    void testGetSupplierById_Success() {
        // Arrange
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toDto(supplier)).thenReturn(responseDto);

        // Act
        SupplierResponseDto result = supplierService.getSupplierById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fournisseur Test", result.getName());
        assertEquals("contact@test.com", result.getContact());

        verify(supplierRepository, times(1)).findById(1L);
        verify(supplierMapper, times(1)).toDto(supplier);
    }

    @Test
    void testGetSupplierById_NotFound_ThrowsException() {
        // Arrange
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            supplierService.getSupplierById(999L);
        });

        assertEquals("Fournisseur non trouvé : ID 999", exception.getMessage());
        verify(supplierRepository, times(1)).findById(999L);
        verify(supplierMapper, never()).toDto(any());
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void testUpdateSupplier_Success() {
        // Arrange
        Supplier updatedSupplier = Supplier.builder()
                .id(1L)
                .name("Fournisseur Modifié")
                .contact("nouveau@test.com")
                .warehouse(warehouse)
                .build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        // CORRECTION : Ne pas utiliser doNothing() ici
        when(supplierRepository.save(any(Supplier.class))).thenReturn(updatedSupplier);

        // Act
        SupplierResponseDto result = supplierService.updateSupplier(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Fournisseur Modifié", result.getName());
        assertEquals("nouveau@test.com", result.getContact());
        assertEquals("Fournisseur mis à jour avec succès", result.getMessage());

        verify(supplierRepository, times(1)).findById(1L);
        verify(supplierMapper, times(1)).toEntityFromUpdate(eq(updateDto), any(Supplier.class));
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void testUpdateSupplier_NotFound_ThrowsException() {
        // Arrange
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            supplierService.updateSupplier(999L, updateDto);
        });

        assertEquals("Fournisseur non trouvé : ID 999", exception.getMessage());
        verify(supplierRepository, times(1)).findById(999L);
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void testUpdateSupplier_WarehouseNotFound_ThrowsException() {
        // Arrange
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(warehouseRepository.existsById(1L)).thenReturn(false);
        // CORRECTION : Supprimer le doNothing()

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            supplierService.updateSupplier(1L, updateDto);
        });

        assertEquals("Entrepôt non trouvé : ID 1", exception.getMessage());
        verify(supplierMapper, times(1)).toEntityFromUpdate(eq(updateDto), any(Supplier.class));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void testUpdateSupplier_WithoutWarehouse_Success() {
        // Arrange
        updateDto.setWarehouseId(null);
        supplier.setWarehouse(null);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        // CORRECTION : Supprimer le doNothing()
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        // Act
        SupplierResponseDto result = supplierService.updateSupplier(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertNull(result.getWarehouseId());
        assertEquals("Fournisseur mis à jour avec succès", result.getMessage());

        verify(supplierMapper, times(1)).toEntityFromUpdate(eq(updateDto), any(Supplier.class));
        verify(warehouseRepository, never()).existsById(any());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    // ==================== DELETE TESTS ====================

    @Test
    void testDeleteSupplier_Success() {
        // Arrange
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        doNothing().when(supplierRepository).delete(supplier);

        // Act
        String result = supplierService.deleteSupplier(1L);

        // Assert
        assertEquals("Fournisseur supprimé avec succès", result);
        verify(supplierRepository, times(1)).findById(1L);
        verify(supplierRepository, times(1)).delete(supplier);
    }

    @Test
    void testDeleteSupplier_NotFound_ThrowsException() {
        // Arrange
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            supplierService.deleteSupplier(999L);
        });

        assertEquals("Fournisseur non trouvé : ID 999", exception.getMessage());
        verify(supplierRepository, times(1)).findById(999L);
        verify(supplierRepository, never()).delete(any());
    }
}