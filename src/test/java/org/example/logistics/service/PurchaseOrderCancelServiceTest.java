package org.example.logistics.service;

import org.example.logistics.dto.purchase.PurchaseOrderCancelDto;
import org.example.logistics.dto.purchase.PurchaseOrderCancelResponseDto;
import org.example.logistics.entity.PurchaseOrder;
import org.example.logistics.entity.Supplier;
import org.example.logistics.entity.Enum.Status;
import org.example.logistics.exception.BadRequestException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.PurchaseOrderCancelMapper;
import org.example.logistics.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Purchase Order Cancel Service Tests")
class PurchaseOrderCancelServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderCancelMapper purchaseOrderCancelMapper;

    @InjectMocks
    private PurchaseOrderCancelService purchaseOrderCancelService;

    private PurchaseOrderCancelDto cancelDto;
    private PurchaseOrder purchaseOrder;
    private PurchaseOrderCancelResponseDto cancelResponseDto;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        supplier = Supplier.builder()
                .id(1L)
                .name("Tech Supplier Inc.")
                .contact("supplier@tech.com")
                .build();

        cancelDto = PurchaseOrderCancelDto.builder()
                .poId(1L)
                .cancelReason("Changement de fournisseur")
                .build();

        purchaseOrder = PurchaseOrder.builder()
                .id(1L)
                .supplier(supplier)
                .status(Status.APPROVED)
                .build();

        cancelResponseDto = PurchaseOrderCancelResponseDto.builder()
                .id(1L)
                .currentStatus("CANCELED")
                .previousStatus("APPROVED")
                .supplierName("Tech Supplier Inc.")
                .build();
    }

    @Test
    @DisplayName("Should cancel purchase order successfully")
    void testCancelPurchaseOrder_Success() {
        // Given
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            assertEquals(Status.CANCELED, po.getStatus());
            assertNotNull(po.getCanceledAt());
            return po;
        });
        when(purchaseOrderCancelMapper.toDto(any(PurchaseOrder.class))).thenReturn(cancelResponseDto);

        // When
        PurchaseOrderCancelResponseDto result = purchaseOrderCancelService.cancelPurchaseOrder(cancelDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("CANCELED", result.getCurrentStatus());
        assertEquals("APPROVED", result.getPreviousStatus());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("annulé avec succès"));

        verify(purchaseOrderRepository, times(1)).findById(1L);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when PO not found")
    void testCancelPurchaseOrder_PONotFound() {
        // Given
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            purchaseOrderCancelService.cancelPurchaseOrder(cancelDto);
        });

        verify(purchaseOrderRepository, times(1)).findById(1L);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @ParameterizedTest
    @DisplayName("Should throw BadRequestException for non-APPROVED status")
    @EnumSource(value = Status.class, names = {"RECEIVED", "CANCELED", "CREATED", "RESERVED",
            "SHIPPED", "DELIVERED", "PARTIAL_RESERVED"})
    void testCancelPurchaseOrder_InvalidStatus(Status status) {
        // Given
        purchaseOrder.setStatus(status);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            purchaseOrderCancelService.cancelPurchaseOrder(cancelDto);
        });

        assertTrue(exception.getMessage().contains("ne peut pas être annulé"));
        assertTrue(exception.getMessage().contains(status.toString()));

        verify(purchaseOrderRepository, times(1)).findById(1L);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should set canceledAt timestamp")
    void testCancelPurchaseOrder_SetsCanceledAt() {
        // Given
        LocalDateTime beforeCancel = LocalDateTime.now();
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            assertNotNull(po.getCanceledAt());
            assertTrue(po.getCanceledAt().isAfter(beforeCancel) ||
                    po.getCanceledAt().isEqual(beforeCancel));
            return po;
        });
        when(purchaseOrderCancelMapper.toDto(any(PurchaseOrder.class))).thenReturn(cancelResponseDto);

        // When
        PurchaseOrderCancelResponseDto result = purchaseOrderCancelService.cancelPurchaseOrder(cancelDto);

        // Then
        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should get canceled purchase orders between dates")
    void testGetCanceledPurchaseOrdersBetween() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);

        PurchaseOrder po1 = PurchaseOrder.builder()
                .id(1L)
                .status(Status.CANCELED)
                .canceledAt(LocalDateTime.of(2025, 6, 15, 10, 30))
                .build();

        PurchaseOrder po2 = PurchaseOrder.builder()
                .id(2L)
                .status(Status.CANCELED)
                .canceledAt(LocalDateTime.of(2025, 8, 20, 14, 45))
                .build();

        List<PurchaseOrder> canceledOrders = Arrays.asList(po1, po2);
        when(purchaseOrderRepository.findCanceledPurchaseOrdersBetween(startDate, endDate))
                .thenReturn(canceledOrders);

        // When
        List<PurchaseOrder> result = purchaseOrderCancelService
                .getCanceledPurchaseOrdersBetween(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Status.CANCELED, result.get(0).getStatus());
        assertEquals(Status.CANCELED, result.get(1).getStatus());

        verify(purchaseOrderRepository, times(1))
                .findCanceledPurchaseOrdersBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should return empty list when no canceled orders in date range")
    void testGetCanceledPurchaseOrdersBetween_EmptyResult() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);

        when(purchaseOrderRepository.findCanceledPurchaseOrdersBetween(startDate, endDate))
                .thenReturn(Collections.emptyList());

        // When
        List<PurchaseOrder> result = purchaseOrderCancelService
                .getCanceledPurchaseOrdersBetween(startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(purchaseOrderRepository, times(1))
                .findCanceledPurchaseOrdersBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should count canceled purchase orders")
    void testCountCanceledPurchaseOrders() {
        // Given
        when(purchaseOrderRepository.countByStatus(Status.CANCELED)).thenReturn(15L);

        // When
        long count = purchaseOrderCancelService.countCanceledPurchaseOrders();

        // Then
        assertEquals(15L, count);
        verify(purchaseOrderRepository, times(1)).countByStatus(Status.CANCELED);
    }

    @Test
    @DisplayName("Should return zero when no canceled orders")
    void testCountCanceledPurchaseOrders_Zero() {
        // Given
        when(purchaseOrderRepository.countByStatus(Status.CANCELED)).thenReturn(0L);

        // When
        long count = purchaseOrderCancelService.countCanceledPurchaseOrders();

        // Then
        assertEquals(0L, count);
        verify(purchaseOrderRepository, times(1)).countByStatus(Status.CANCELED);
    }

    @Test
    @DisplayName("Should include reason in cancel DTO")
    void testCancelPurchaseOrder_WithReason() {
        // Given
        cancelDto.setCancelReason("Supplier changed pricing");
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderCancelMapper.toDto(any(PurchaseOrder.class))).thenReturn(cancelResponseDto);

        // When
        PurchaseOrderCancelResponseDto result = purchaseOrderCancelService.cancelPurchaseOrder(cancelDto);

        // Then
        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should preserve previous status in response")
    void testCancelPurchaseOrder_PreservesPreviousStatus() {
        // Given
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderCancelMapper.toDto(any(PurchaseOrder.class))).thenReturn(cancelResponseDto);

        // When
        PurchaseOrderCancelResponseDto result = purchaseOrderCancelService.cancelPurchaseOrder(cancelDto);

        // Then
        assertEquals("APPROVED", result.getPreviousStatus());
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should handle multiple canceled orders query")
    void testGetCanceledPurchaseOrdersBetween_MultipleOrders() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);

        List<PurchaseOrder> orders = Arrays.asList(
                PurchaseOrder.builder().id(1L).status(Status.CANCELED).build(),
                PurchaseOrder.builder().id(2L).status(Status.CANCELED).build(),
                PurchaseOrder.builder().id(3L).status(Status.CANCELED).build()
        );

        when(purchaseOrderRepository.findCanceledPurchaseOrdersBetween(startDate, endDate))
                .thenReturn(orders);

        // When
        List<PurchaseOrder> result = purchaseOrderCancelService
                .getCanceledPurchaseOrdersBetween(startDate, endDate);

        // Then
        assertEquals(3, result.size());
        verify(purchaseOrderRepository, times(1))
                .findCanceledPurchaseOrdersBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should update status correctly from APPROVED to CANCELED")
    void testCancelPurchaseOrder_StatusTransition() {
        // Given
        assertEquals(Status.APPROVED, purchaseOrder.getStatus());
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            assertEquals(Status.CANCELED, po.getStatus());
            return po;
        });
        when(purchaseOrderCancelMapper.toDto(any(PurchaseOrder.class))).thenReturn(cancelResponseDto);

        // When
        purchaseOrderCancelService.cancelPurchaseOrder(cancelDto);

        // Then
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should handle large number of canceled orders")
    void testCountCanceledPurchaseOrders_LargeNumber() {
        // Given
        when(purchaseOrderRepository.countByStatus(Status.CANCELED)).thenReturn(10000L);

        // When
        long count = purchaseOrderCancelService.countCanceledPurchaseOrders();

        // Then
        assertEquals(10000L, count);
    }

    @Test
    @DisplayName("Should return correct message format")
    void testCancelPurchaseOrder_MessageFormat() {
        // Given
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderCancelMapper.toDto(any(PurchaseOrder.class))).thenReturn(cancelResponseDto);

        // When
        PurchaseOrderCancelResponseDto result = purchaseOrderCancelService.cancelPurchaseOrder(cancelDto);

        // Then
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Purchase Order #1"));
        assertTrue(result.getMessage().contains("annulé avec succès"));
    }
}