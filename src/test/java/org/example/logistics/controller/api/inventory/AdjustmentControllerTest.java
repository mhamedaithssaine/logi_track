package org.example.logistics.controller.api.inventory;

import org.example.logistics.dto.inventory.AdjustmentCreateDto;
import org.example.logistics.dto.inventory.AdjustmentResponseDto;
import org.example.logistics.service.AdjustmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Adjustment Controller Tests")
class AdjustmentControllerTest {

    @Mock
    private AdjustmentService adjustmentService;

    @InjectMocks
    private AdjustmentController adjustmentController;

    private AdjustmentCreateDto createDto;
    private AdjustmentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createDto = new AdjustmentCreateDto();
        createDto.setProductId(1L);
        createDto.setWarehouseId(1L);
        createDto.setAdjustmentQty(10);
        createDto.setReason("Correction d'inventaire");

        responseDto = new AdjustmentResponseDto();
        responseDto.setId(1L);
        responseDto.setType("ADJUSTMENT");
        responseDto.setAdjustmentApplied(10);
        responseDto.setNewQtyOnHand(110);
        responseDto.setMessage("Ajustement effectué avec succès");
        responseDto.setOccurredAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should adjust stock successfully with positive quantity")
    void testAdjustStock_PositiveQuantity_Success() {
        // Given
        when(adjustmentService.adjustStock(any(AdjustmentCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<AdjustmentResponseDto> response = adjustmentController.adjustStock(createDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("ADJUSTMENT", response.getBody().getType());
        assertEquals(10, response.getBody().getAdjustmentApplied());
        assertEquals(110, response.getBody().getNewQtyOnHand());

        verify(adjustmentService, times(1)).adjustStock(any(AdjustmentCreateDto.class));
    }

    @Test
    @DisplayName("Should adjust stock successfully with negative quantity")
    void testAdjustStock_NegativeQuantity_Success() {
        // Given
        createDto.setAdjustmentQty(-5);
        responseDto.setAdjustmentApplied(-5);
        responseDto.setNewQtyOnHand(95);
        when(adjustmentService.adjustStock(any(AdjustmentCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<AdjustmentResponseDto> response = adjustmentController.adjustStock(createDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(-5, response.getBody().getAdjustmentApplied());
        assertEquals(95, response.getBody().getNewQtyOnHand());

        verify(adjustmentService, times(1)).adjustStock(any(AdjustmentCreateDto.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testAdjustStock_ProductNotFound() {
        // Given
        when(adjustmentService.adjustStock(any(AdjustmentCreateDto.class)))
                .thenThrow(new RuntimeException("Produit introuvable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adjustmentController.adjustStock(createDto);
        });

        verify(adjustmentService, times(1)).adjustStock(any(AdjustmentCreateDto.class));
    }

    @Test
    @DisplayName("Should throw exception when warehouse not found")
    void testAdjustStock_WarehouseNotFound() {
        // Given
        when(adjustmentService.adjustStock(any(AdjustmentCreateDto.class)))
                .thenThrow(new RuntimeException("Entrepôt introuvable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adjustmentController.adjustStock(createDto);
        });

        verify(adjustmentService, times(1)).adjustStock(any(AdjustmentCreateDto.class));
    }

    @Test
    @DisplayName("Should throw exception when adjustment would result in negative stock")
    void testAdjustStock_NegativeStock() {
        // Given
        createDto.setAdjustmentQty(-200);
        when(adjustmentService.adjustStock(any(AdjustmentCreateDto.class)))
                .thenThrow(new RuntimeException("L'ajustement résulterait en stock négatif"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adjustmentController.adjustStock(createDto);
        });

        verify(adjustmentService, times(1)).adjustStock(any(AdjustmentCreateDto.class));
    }

    @Test
    @DisplayName("Should handle zero adjustment quantity")
    void testAdjustStock_ZeroQuantity() {
        // Given
        createDto.setAdjustmentQty(0);
        responseDto.setAdjustmentApplied(0);
        responseDto.setNewQtyOnHand(100);
        when(adjustmentService.adjustStock(any(AdjustmentCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<AdjustmentResponseDto> response = adjustmentController.adjustStock(createDto);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getBody().getAdjustmentApplied());

        verify(adjustmentService, times(1)).adjustStock(any(AdjustmentCreateDto.class));
    }

    @Test
    @DisplayName("Should include reason in adjustment")
    void testAdjustStock_WithReason() {
        // Given
        createDto.setReason("Inventaire physique annuel");
        when(adjustmentService.adjustStock(any(AdjustmentCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<AdjustmentResponseDto> response = adjustmentController.adjustStock(createDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(adjustmentService, times(1)).adjustStock(any(AdjustmentCreateDto.class));
    }
}