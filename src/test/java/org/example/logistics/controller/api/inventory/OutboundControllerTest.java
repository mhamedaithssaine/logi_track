package org.example.logistics.controller.api.inventory;

import org.example.logistics.dto.inventory.OutboundCreateDto;
import org.example.logistics.dto.inventory.OutboundResponseDto;
import org.example.logistics.service.OutboundService;
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

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Outbound Controller Tests")
class OutboundControllerTest {

    @Mock
    private OutboundService outboundService;

    @InjectMocks
    private OutboundController outboundController;

    private OutboundCreateDto createDto;
    private OutboundResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createDto = new OutboundCreateDto();
        createDto.setProductId(1L);
        createDto.setWarehouseId(1L);
        createDto.setQuantity(30);
        createDto.setReferenceDoc("SO-2025-001");

        responseDto = new OutboundResponseDto();
        responseDto.setId(1L);
        responseDto.setType("OUTBOUND");
        responseDto.setQuantitySubtracted(30);
        responseDto.setNewQtyOnHand(70);
        responseDto.setMessage("Sortie enregistrée avec succès");
        responseDto.setOccurredAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should record outbound successfully")
    void testRecordOutbound_Success() {
        when(outboundService.recordOutbound(any(OutboundCreateDto.class))).thenReturn(responseDto);

        ResponseEntity<OutboundResponseDto> response = outboundController.recordOutbound(createDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("OUTBOUND", response.getBody().getType());
        assertEquals(30, response.getBody().getQuantitySubtracted());
        assertEquals(70, response.getBody().getNewQtyOnHand());

        verify(outboundService, times(1)).recordOutbound(any(OutboundCreateDto.class));
    }

    @Test
    @DisplayName("Should record outbound with reference document")
    void testRecordOutbound_WithReferenceDoc() {
        createDto.setReferenceDoc("SO-2025-999");
        when(outboundService.recordOutbound(any(OutboundCreateDto.class))).thenReturn(responseDto);

        ResponseEntity<OutboundResponseDto> response = outboundController.recordOutbound(createDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(outboundService, times(1)).recordOutbound(any(OutboundCreateDto.class));
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for various error scenarios")
    @MethodSource("provideErrorScenarios")
    void testRecordOutbound_ErrorScenarios(String errorMessage) {
        // Given
        when(outboundService.recordOutbound(any(OutboundCreateDto.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            outboundController.recordOutbound(createDto);
        });

        verify(outboundService, times(1)).recordOutbound(any(OutboundCreateDto.class));
    }

    private static Stream<Arguments> provideErrorScenarios() {
        return Stream.of(
                Arguments.of("Produit introuvable"),
                Arguments.of("Entrepôt introuvable"),
                Arguments.of("Stock insuffisant")
        );
    }

    @Test
    @DisplayName("Should throw exception when inventory not found")
    void testRecordOutbound_InventoryNotFound() {
        when(outboundService.recordOutbound(any(OutboundCreateDto.class)))
                .thenThrow(new RuntimeException("Inventaire introuvable"));

        assertThrows(RuntimeException.class, () -> {
            outboundController.recordOutbound(createDto);
        });

        verify(outboundService, times(1)).recordOutbound(any(OutboundCreateDto.class));
    }

    @Test
    @DisplayName("Should handle outbound that depletes stock")
    void testRecordOutbound_DepleteStock() {
        createDto.setQuantity(100);
        responseDto.setQuantitySubtracted(100);
        responseDto.setNewQtyOnHand(0);
        responseDto.setMessage("Stock épuisé");
        when(outboundService.recordOutbound(any(OutboundCreateDto.class))).thenReturn(responseDto);

        ResponseEntity<OutboundResponseDto> response = outboundController.recordOutbound(createDto);

        assertNotNull(response);
        assertEquals(0, response.getBody().getNewQtyOnHand());

        verify(outboundService, times(1)).recordOutbound(any(OutboundCreateDto.class));
    }

    @Test
    @DisplayName("Should handle multiple outbound operations")
    void testRecordOutbound_MultipleOperations() {
        when(outboundService.recordOutbound(any(OutboundCreateDto.class))).thenReturn(responseDto);

        ResponseEntity<OutboundResponseDto> response1 = outboundController.recordOutbound(createDto);
        ResponseEntity<OutboundResponseDto> response2 = outboundController.recordOutbound(createDto);

        assertNotNull(response1);
        assertNotNull(response2);

        verify(outboundService, times(2)).recordOutbound(any(OutboundCreateDto.class));
    }

    @Test
    @DisplayName("Should validate minimum quantity")
    void testRecordOutbound_MinimumQuantity() {
        createDto.setQuantity(1);
        responseDto.setQuantitySubtracted(1);
        responseDto.setNewQtyOnHand(99);
        when(outboundService.recordOutbound(any(OutboundCreateDto.class))).thenReturn(responseDto);

        ResponseEntity<OutboundResponseDto> response = outboundController.recordOutbound(createDto);

        assertNotNull(response);
        assertEquals(1, response.getBody().getQuantitySubtracted());

        verify(outboundService, times(1)).recordOutbound(any(OutboundCreateDto.class));
    }
}