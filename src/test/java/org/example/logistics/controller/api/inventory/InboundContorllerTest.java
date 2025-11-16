package org.example.logistics.controller.api.inventory;

import org.example.logistics.dto.inventory.InboundCreateDto;
import org.example.logistics.dto.inventory.InboundResponseDto;
import org.example.logistics.service.InboundService;
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

@DisplayName("Inbound Controller Tests")
class InboundControllerTest {

    @Mock
    private InboundService inboundService;

    @InjectMocks
    private InboundController inboundController;

    private InboundCreateDto createDto;
    private InboundResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createDto = new InboundCreateDto();
        createDto.setProductId(1L);
        createDto.setWarehouseId(1L);
        createDto.setQuantity(50);
        createDto.setReferenceDoc("PO-2025-001");

        responseDto = InboundResponseDto.builder()
                .id(1L)
                .productId(1L)
                .warehouseId(1L)
                .type("INBOUND")
                .quantityAdded(50)
                .newQtyOnHand(150.0)
                .message("Réception enregistrée avec succès")
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should record inbound successfully")
    void testRecordInbound_Success() {
        // Given
        when(inboundService.recordInbound(any(InboundCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<InboundResponseDto> response = inboundController.recordInbound(createDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("INBOUND", response.getBody().getType());
        assertEquals(50, response.getBody().getQuantityAdded());
        assertEquals(150.0, response.getBody().getNewQtyOnHand());
        assertEquals(1L, response.getBody().getProductId());
        assertEquals(1L, response.getBody().getWarehouseId());

        verify(inboundService, times(1)).recordInbound(any(InboundCreateDto.class));
    }

    @Test
    @DisplayName("Should record inbound with reference document")
    void testRecordInbound_WithReferenceDoc() {
        // Given
        createDto.setReferenceDoc("PO-2025-999");
        when(inboundService.recordInbound(any(InboundCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<InboundResponseDto> response = inboundController.recordInbound(createDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(inboundService, times(1)).recordInbound(any(InboundCreateDto.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testRecordInbound_ProductNotFound() {
        // Given
        when(inboundService.recordInbound(any(InboundCreateDto.class)))
                .thenThrow(new RuntimeException("Produit introuvable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inboundController.recordInbound(createDto);
        });

        verify(inboundService, times(1)).recordInbound(any(InboundCreateDto.class));
    }

    @Test
    @DisplayName("Should throw exception when warehouse not found")
    void testRecordInbound_WarehouseNotFound() {
        // Given
        when(inboundService.recordInbound(any(InboundCreateDto.class)))
                .thenThrow(new RuntimeException("Entrepôt introuvable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inboundController.recordInbound(createDto);
        });

        verify(inboundService, times(1)).recordInbound(any(InboundCreateDto.class));
    }

    @Test
    @DisplayName("Should handle large quantity inbound")
    void testRecordInbound_LargeQuantity() {
        // Given
        createDto.setQuantity(10000);
        responseDto = InboundResponseDto.builder()
                .id(2L)
                .quantityAdded(10000)
                .newQtyOnHand(10100.0)
                .type("INBOUND")
                .message("Grande réception enregistrée")
                .occurredAt(LocalDateTime.now())
                .build();
        when(inboundService.recordInbound(any(InboundCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<InboundResponseDto> response = inboundController.recordInbound(createDto);

        // Then
        assertNotNull(response);
        assertEquals(10000, response.getBody().getQuantityAdded());
        assertEquals(10100.0, response.getBody().getNewQtyOnHand());

        verify(inboundService, times(1)).recordInbound(any(InboundCreateDto.class));
    }

    @Test
    @DisplayName("Should create inventory if not exists")
    void testRecordInbound_CreateInventory() {
        // Given
        responseDto.setNewQtyOnHand(50.0);
        responseDto.setMessage("Inventaire créé et réception enregistrée");
        when(inboundService.recordInbound(any(InboundCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<InboundResponseDto> response = inboundController.recordInbound(createDto);

        // Then
        assertNotNull(response);
        assertEquals(50.0, response.getBody().getNewQtyOnHand());

        verify(inboundService, times(1)).recordInbound(any(InboundCreateDto.class));
    }

    @Test
    @DisplayName("Should handle multiple inbound operations")
    void testRecordInbound_MultipleOperations() {
        // Given
        when(inboundService.recordInbound(any(InboundCreateDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<InboundResponseDto> response1 = inboundController.recordInbound(createDto);
        ResponseEntity<InboundResponseDto> response2 = inboundController.recordInbound(createDto);

        // Then
        assertNotNull(response1);
        assertNotNull(response2);

        verify(inboundService, times(2)).recordInbound(any(InboundCreateDto.class));
    }
}