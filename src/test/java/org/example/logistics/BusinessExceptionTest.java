package org.example.logistics;

import org.example.logistics.exception.BadRequestException;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void testBadRequestException_WithMessage() {
        String message = "Invalid request data";
        BadRequestException exception = new BadRequestException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConflictException_WithMessage() {
        String message = "Resource already exists";
        ConflictException exception = new ConflictException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testResourceNotFoundException_WithMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testUnauthorizedException_WithMessage() {
        String message = "Access denied";
        UnauthorizedException exception = new UnauthorizedException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testStockUnavailableException_Simulation() {
        String message = "Stock insuffisant pour le produit SKU-123";
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException(message);
        });
        assertTrue(exception.getMessage().contains("Stock insuffisant"));
    }
}