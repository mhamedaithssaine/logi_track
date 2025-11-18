package org.example.logistics.service;

import org.example.logistics.dto.warehouse.WarehouseCreateDto;
import org.example.logistics.dto.warehouse.WarehouseResponseDto;
import org.example.logistics.dto.warehouse.WarehouseUpdateDto;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.mapper.WarehouseMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse warehouse;
    private WarehouseCreateDto createDto;
    private WarehouseUpdateDto updateDto;
    private WarehouseResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Setup Warehouse
        warehouse = Warehouse.builder()
                .id(1L)
                .code("WH001")
                .name("Entrepôt Principal")
                .build();

        // Setup CreateDto
        createDto = WarehouseCreateDto.builder()
                .code("WH001")
                .name("Entrepôt Principal")
                .build();

        // Setup UpdateDto
        updateDto = WarehouseUpdateDto.builder()
                .name("Entrepôt Modifié")
                .build();

        // Setup ResponseDto
        responseDto = WarehouseResponseDto.builder()
                .id(1L)
                .code("WH001")
                .name("Entrepôt Principal")
                .build();
    }

    // ==================== CREATE TESTS ====================

    @Test
    void testCreateWarehouse_Success() {
        // Arrange
        when(warehouseRepository.existsByCode("WH001")).thenReturn(false);
        when(warehouseMapper.toEntity(createDto)).thenReturn(warehouse);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        WarehouseResponseDto result = warehouseService.createWarehouse(createDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("WH001", result.getCode());
        assertEquals("Entrepôt Principal", result.getName());
        assertEquals("Entrepôt créé avec succès", result.getMessage());

        verify(warehouseRepository, times(1)).existsByCode("WH001");
        verify(warehouseMapper, times(1)).toEntity(createDto);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void testCreateWarehouse_CodeAlreadyExists_ThrowsException() {
        // Arrange
        when(warehouseRepository.existsByCode("WH001")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            warehouseService.createWarehouse(createDto);
        });

        assertEquals("Code entrepôt existe déjà", exception.getMessage());
        verify(warehouseRepository, times(1)).existsByCode("WH001");
        verify(warehouseMapper, never()).toEntity(any());
        verify(warehouseRepository, never()).save(any());
    }

    // ==================== READ ALL TESTS ====================

    @Test
    void testGetAllWarehouses_Success() {
        // Arrange
        Warehouse warehouse2 = Warehouse.builder()
                .id(2L)
                .code("WH002")
                .name("Entrepôt Secondaire")
                .build();

        List<Warehouse> warehouses = Arrays.asList(warehouse, warehouse2);

        WarehouseResponseDto responseDto2 = WarehouseResponseDto.builder()
                .id(2L)
                .code("WH002")
                .name("Entrepôt Secondaire")
                .build();

        when(warehouseRepository.findAll()).thenReturn(warehouses);
        when(warehouseMapper.toDto(warehouse)).thenReturn(responseDto);
        when(warehouseMapper.toDto(warehouse2)).thenReturn(responseDto2);

        // Act
        List<WarehouseResponseDto> result = warehouseService.getAllWarehouses();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("WH001", result.get(0).getCode());
        assertEquals("Entrepôt Principal", result.get(0).getName());
        assertEquals("WH002", result.get(1).getCode());
        assertEquals("Entrepôt Secondaire", result.get(1).getName());

        verify(warehouseRepository, times(1)).findAll();
        verify(warehouseMapper, times(2)).toDto(any(Warehouse.class));
    }

    @Test
    void testGetAllWarehouses_EmptyList() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<WarehouseResponseDto> result = warehouseService.getAllWarehouses();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(warehouseRepository, times(1)).findAll();
        verify(warehouseMapper, never()).toDto(any());
    }

    // ==================== READ BY CODE TESTS ====================

    @Test
    void testGetWarehouseByCode_Success() {
        // Arrange
        when(warehouseRepository.findByCode("WH001")).thenReturn(Optional.of(warehouse));
        when(warehouseMapper.toDto(warehouse)).thenReturn(responseDto);

        // Act
        WarehouseResponseDto result = warehouseService.getWarehouseByCode("WH001");

        // Assert
        assertNotNull(result);
        assertEquals("WH001", result.getCode());
        assertEquals("Entrepôt Principal", result.getName());

        verify(warehouseRepository, times(1)).findByCode("WH001");
        verify(warehouseMapper, times(1)).toDto(warehouse);
    }

    @Test
    void testGetWarehouseByCode_NotFound_ThrowsException() {
        // Arrange
        when(warehouseRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            warehouseService.getWarehouseByCode("INVALID");
        });

        assertEquals("Entrepôt non trouvé pour code : INVALID", exception.getMessage());
        verify(warehouseRepository, times(1)).findByCode("INVALID");
        verify(warehouseMapper, never()).toDto(any());
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void testUpdateWarehouse_Success() {
        // Arrange
        Warehouse updatedWarehouse = Warehouse.builder()
                .id(1L)
                .code("WH001")
                .name("Entrepôt Modifié")
                .build();

        when(warehouseRepository.findByCode("WH001")).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(updatedWarehouse);

        // Act
        WarehouseResponseDto result = warehouseService.updateWarehouse("WH001", updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("WH001", result.getCode());
        assertEquals("Entrepôt Modifié", result.getName());
        assertEquals("Entrepôt mis à jour avec succès", result.getMessage());

        verify(warehouseRepository, times(1)).findByCode("WH001");
        verify(warehouseMapper, times(1)).toEntityFromUpdate(updateDto, warehouse);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void testUpdateWarehouse_NotFound_ThrowsException() {
        // Arrange
        when(warehouseRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            warehouseService.updateWarehouse("INVALID", updateDto);
        });

        assertEquals("Entrepôt non trouvé pour code : INVALID", exception.getMessage());
        verify(warehouseRepository, times(1)).findByCode("INVALID");
        verify(warehouseMapper, never()).toEntityFromUpdate(any(), any());
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void testUpdateWarehouse_VerifyMapperIsCalled() {
        // Arrange
        when(warehouseRepository.findByCode("WH001")).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        warehouseService.updateWarehouse("WH001", updateDto);

        // Assert - Vérifier que le mapper modifie bien l'entité
        verify(warehouseMapper, times(1)).toEntityFromUpdate(updateDto, warehouse);
    }

    // ==================== DELETE TESTS ====================

    @Test
    void testDeleteWarehouse_Success() {
        // Arrange
        when(warehouseRepository.findByCode("WH001")).thenReturn(Optional.of(warehouse));
        doNothing().when(warehouseRepository).delete(warehouse);

        // Act
        String result = warehouseService.deleteWarehouse("WH001");

        // Assert
        assertEquals("Entrepôt supprimé avec succès", result);
        verify(warehouseRepository, times(1)).findByCode("WH001");
        verify(warehouseRepository, times(1)).delete(warehouse);
    }

    @Test
    void testDeleteWarehouse_NotFound_ThrowsException() {
        // Arrange
        when(warehouseRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            warehouseService.deleteWarehouse("INVALID");
        });

        assertEquals("Entrepôt non trouvé pour code : INVALID", exception.getMessage());
        verify(warehouseRepository, times(1)).findByCode("INVALID");
        verify(warehouseRepository, never()).delete(any());
    }

    // ==================== EDGE CASES TESTS ====================

    @Test
    void testCreateWarehouse_WithSpecialCharacters() {
        // Arrange
        WarehouseCreateDto specialDto = WarehouseCreateDto.builder()
                .code("WH-001-SPEC")
                .name("Entrepôt Spécial & Cie")
                .build();

        Warehouse specialWarehouse = Warehouse.builder()
                .id(1L)
                .code("WH-001-SPEC")
                .name("Entrepôt Spécial & Cie")
                .build();

        when(warehouseRepository.existsByCode("WH-001-SPEC")).thenReturn(false);
        when(warehouseMapper.toEntity(specialDto)).thenReturn(specialWarehouse);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(specialWarehouse);

        // Act
        WarehouseResponseDto result = warehouseService.createWarehouse(specialDto);

        // Assert
        assertNotNull(result);
        assertEquals("WH-001-SPEC", result.getCode());
        assertEquals("Entrepôt Spécial & Cie", result.getName());
    }

    @Test
    void testGetAllWarehouses_WithMultipleWarehouses() {
        // Arrange
        List<Warehouse> warehouses = Arrays.asList(
                Warehouse.builder().id(1L).code("WH001").name("Entrepôt 1").build(),
                Warehouse.builder().id(2L).code("WH002").name("Entrepôt 2").build(),
                Warehouse.builder().id(3L).code("WH003").name("Entrepôt 3").build(),
                Warehouse.builder().id(4L).code("WH004").name("Entrepôt 4").build()
        );

        when(warehouseRepository.findAll()).thenReturn(warehouses);
        when(warehouseMapper.toDto(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse w = invocation.getArgument(0);
            return WarehouseResponseDto.builder()
                    .id(w.getId())
                    .code(w.getCode())
                    .name(w.getName())
                    .build();
        });

        // Act
        List<WarehouseResponseDto> result = warehouseService.getAllWarehouses();

        // Assert
        assertEquals(4, result.size());
        verify(warehouseMapper, times(4)).toDto(any(Warehouse.class));
    }

    @Test
    void testUpdateWarehouse_OnlyNameChange() {
        // Arrange
        WarehouseUpdateDto minimalUpdate = WarehouseUpdateDto.builder()
                .name("Nouveau Nom")
                .build();

        when(warehouseRepository.findByCode("WH001")).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        WarehouseResponseDto result = warehouseService.updateWarehouse("WH001", minimalUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("WH001", result.getCode());
        verify(warehouseMapper, times(1)).toEntityFromUpdate(minimalUpdate, warehouse);
    }
}