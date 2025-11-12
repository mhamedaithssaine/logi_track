package org.example.logistics;

import org.example.logistics.dto.order.SalesOrderReserveDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSalesOrderReserveDto_ValidData() {
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(1L)
                .build();

        Set<ConstraintViolation<SalesOrderReserveDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testSalesOrderReserveDto_NullOrderId() {
        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(null)
                .build();

        Set<ConstraintViolation<SalesOrderReserveDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testDtoBuilder_ShouldCreateCorrectly() {
        Long orderId = 123L;

        SalesOrderReserveDto dto = SalesOrderReserveDto.builder()
                .orderId(orderId)
                .build();

        assertNotNull(dto);
        assertEquals(orderId, dto.getOrderId());
    }

    @Test
    void testDtoEquality_ShouldWorkCorrectly() {
        SalesOrderReserveDto dto1 = SalesOrderReserveDto.builder()
                .orderId(1L)
                .build();

        SalesOrderReserveDto dto2 = SalesOrderReserveDto.builder()
                .orderId(1L)
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testDtoValidation_ShouldHandleInvalidData() {
        assertThrows(NullPointerException.class, () -> {
            SalesOrderReserveDto.builder()
                    .orderId(null)
                    .build()
                    .getOrderId()
                    .intValue();
        });
    }
}