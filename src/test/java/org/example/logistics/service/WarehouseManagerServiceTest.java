package org.example.logistics.service;

import org.example.logistics.dto.warehousemanager.WarehouseManagerLoginDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerRegisterDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerResponseDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerUpdateDto;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.entity.WarehouseManager;
import org.example.logistics.entity.Enum.Role;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.WarehouseManagerMapper;
import org.example.logistics.repository.RefreshTokenRepository;
import org.example.logistics.repository.WarehouseManagerRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseManagerServiceTest {

    @Mock
    private WarehouseManagerRepository warehouseManagerRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private WarehouseManagerMapper warehouseManagerMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private WarehouseManagerService warehouseManagerService;

    private Warehouse warehouse;
    private WarehouseManager warehouseManager;
    private WarehouseManagerRegisterDto registerDto;
    private WarehouseManagerLoginDto loginDto;
    private WarehouseManagerUpdateDto updateDto;
    private WarehouseManagerResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Setup Warehouse
        warehouse = Warehouse.builder()
                .id(1L)
                .code("WH001")
                .name("Entrepôt Principal")
                .build();

        // Setup WarehouseManager
        warehouseManager = WarehouseManager.builder()
                .id(1L)
                .name("John Doe")
                .email("john@test.com")
                .passwordHash("$2a$10$encodedPassword")
                .role(Role.WAREHOUSE_MANAGER)
                .active(true)
                .phone("0612345678")
                .warehouse(warehouse)
                .build();

        // Setup RegisterDto
        registerDto = WarehouseManagerRegisterDto.builder()
                .name("John Doe")
                .email("john@test.com")
                .password("password123")
                .phone("0612345678")
                .warehouseId(1L)
                .build();

        // Setup LoginDto
        loginDto = WarehouseManagerLoginDto.builder()
                .email("john@test.com")
                .password("password123")
                .build();

        // Setup UpdateDto
        updateDto = WarehouseManagerUpdateDto.builder()
                .name("John Updated")
                .phone("0698765432")
                .warehouseId(1L)
                .build();

        // Setup ResponseDto
        responseDto = WarehouseManagerResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@test.com")
                .phone("0612345678")
                .role("WAREHOUSE_MANAGER")
                .active(true)
                .warehouseId(1L)
                .warehouseName("Entrepôt Principal")
                .build();
    }

    // ==================== REGISTER TESTS ====================

    @Test
    void testRegister_Success() {
        // Arrange
        when(warehouseManagerRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseManagerMapper.toEntity(registerDto)).thenReturn(warehouseManager);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(warehouseManagerRepository.save(any(WarehouseManager.class))).thenReturn(warehouseManager);
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        // Act
        WarehouseManagerResponseDto result = warehouseManagerService.register(registerDto);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@test.com", result.getEmail());
        assertEquals("0612345678", result.getPhone());
        assertEquals(1L, result.getWarehouseId());
        assertEquals("Warehouse Manager créé avec succès", result.getMessage());

        verify(warehouseManagerRepository, times(1)).existsByEmail("john@test.com");
        verify(warehouseRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(warehouseManagerRepository, times(1)).save(any(WarehouseManager.class));
    }

    @Test
    void testRegister_EmailAlreadyExists_ThrowsConflictException() {
        // Arrange
        when(warehouseManagerRepository.existsByEmail("john@test.com")).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            warehouseManagerService.register(registerDto);
        });

        assertEquals("Email déjà utilisé : john@test.com", exception.getMessage());
        verify(warehouseManagerRepository, times(1)).existsByEmail("john@test.com");
        verify(warehouseRepository, never()).findById(any());
        verify(warehouseManagerRepository, never()).save(any());
    }

    @Test
    void testRegister_WarehouseNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(warehouseManagerRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            warehouseManagerService.register(registerDto);
        });

        assertTrue(exception.getMessage().contains("Warehouse"));
        verify(warehouseManagerRepository, times(1)).existsByEmail("john@test.com");
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseManagerRepository, never()).save(any());
    }

    @Test
    void testRegister_WithNullPassword_Success() {
        // Arrange
        registerDto.setPassword(null);
        when(warehouseManagerRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseManagerMapper.toEntity(registerDto)).thenReturn(warehouseManager);
        when(warehouseManagerRepository.save(any(WarehouseManager.class))).thenReturn(warehouseManager);
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        // Act
        WarehouseManagerResponseDto result = warehouseManagerService.register(registerDto);

        // Assert
        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
        verify(warehouseManagerRepository, times(1)).save(any(WarehouseManager.class));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void testLogin_Success() {
        // Arrange
        when(warehouseManagerRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(warehouseManager));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword"))
                .thenReturn(true);
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        // Act
        WarehouseManagerResponseDto result = warehouseManagerService.login(loginDto);

        // Assert
        assertNotNull(result);
        assertEquals("john@test.com", result.getEmail());
        assertEquals("Authentification réussie", result.getMessage());

        verify(warehouseManagerRepository, times(1)).findByEmail("john@test.com");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$encodedPassword");
    }

    @Test
    void testLogin_EmailNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(warehouseManagerRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            warehouseManagerService.login(loginDto);
        });

        assertTrue(exception.getMessage().contains("WarehouseManager"));
        verify(warehouseManagerRepository, times(1)).findByEmail("john@test.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_IncorrectPassword_ThrowsConflictException() {
        // Arrange
        when(warehouseManagerRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(warehouseManager));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword"))
                .thenReturn(false);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            warehouseManagerService.login(loginDto);
        });

        assertEquals("Mot de passe incorrect", exception.getMessage());
        verify(warehouseManagerRepository, times(1)).findByEmail("john@test.com");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$encodedPassword");
    }

    // ==================== GET BY ID TESTS ====================

    @Test
    void testGetById_Success() {
        // Arrange
        when(warehouseManagerRepository.findById(1L)).thenReturn(Optional.of(warehouseManager));
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        // Act
        WarehouseManagerResponseDto result = warehouseManagerService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());

        verify(warehouseManagerRepository, times(1)).findById(1L);
        verify(warehouseManagerMapper, times(1)).toDto(warehouseManager);
    }

    @Test
    void testGetById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(warehouseManagerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            warehouseManagerService.getById(999L);
        });

        assertTrue(exception.getMessage().contains("WarehouseManager"));
        verify(warehouseManagerRepository, times(1)).findById(999L);
        verify(warehouseManagerMapper, never()).toDto(any());
    }

    // ==================== GET BY EMAIL TESTS ====================

    @Test
    void testGetByEmail_Success() {
        // Arrange
        when(warehouseManagerRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(warehouseManager));
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        // Act
        WarehouseManagerResponseDto result = warehouseManagerService.getByEmail("john@test.com");

        // Assert
        assertNotNull(result);
        assertEquals("john@test.com", result.getEmail());

        verify(warehouseManagerRepository, times(1)).findByEmail("john@test.com");
        verify(warehouseManagerMapper, times(1)).toDto(warehouseManager);
    }

    @Test
    void testGetByEmail_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(warehouseManagerRepository.findByEmail("notfound@test.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            warehouseManagerService.getByEmail("notfound@test.com");
        });

        assertTrue(exception.getMessage().contains("WarehouseManager"));
        verify(warehouseManagerRepository, times(1)).findByEmail("notfound@test.com");
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void testUpdate_Success() {
        // Arrange
        when(warehouseManagerRepository.findById(1L)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseManagerRepository.save(any(WarehouseManager.class)))
                .thenReturn(warehouseManager);
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        // Act
        WarehouseManagerResponseDto result = warehouseManagerService.update(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Warehouse Manager mis à jour", result.getMessage());

        verify(warehouseManagerRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseManagerRepository, times(1)).save(any(WarehouseManager.class));
    }

    @Test
    void testUpdate_WithNullFields_OnlyUpdatesNonNullFields() {
        // Arrange
        updateDto.setName(null);
        updateDto.setPhone(null);
        updateDto.setWarehouseId(null);

        when(warehouseManagerRepository.findById(1L)).thenReturn(Optional.of(warehouseManager));
        when(warehouseManagerRepository.save(any(WarehouseManager.class)))
                .thenReturn(warehouseManager);
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        // Act
        WarehouseManagerResponseDto result = warehouseManagerService.update(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(warehouseRepository, never()).findById(any());
        verify(warehouseManagerRepository, times(1)).save(any(WarehouseManager.class));
    }

    @Test
    void testUpdate_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(warehouseManagerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            warehouseManagerService.update(999L, updateDto);
        });

        assertTrue(exception.getMessage().contains("WarehouseManager"));
        verify(warehouseManagerRepository, times(1)).findById(999L);
        verify(warehouseManagerRepository, never()).save(any());
    }

    @Test
    void testUpdate_WarehouseNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(warehouseManagerRepository.findById(1L)).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            warehouseManagerService.update(1L, updateDto);
        });

        assertTrue(exception.getMessage().contains("Warehouse"));
        verify(warehouseManagerRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseManagerRepository, never()).save(any());
    }

    // ==================== DELETE TESTS ====================

    @Test
    void testDelete_Success() {
        // Arrange
        when(warehouseManagerRepository.findById(1L)).thenReturn(Optional.of(warehouseManager));
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);
        doNothing().when(refreshTokenRepository).deleteAllByUserId(1L);
        doNothing().when(warehouseManagerRepository).deleteById(1L);

        // Act
        WarehouseManagerResponseDto result = warehouseManagerService.delete(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Warehouse Manager supprimé avec succès", result.getMessage());

        verify(warehouseManagerRepository, times(1)).findById(1L);
        verify(refreshTokenRepository, times(1)).deleteAllByUserId(1L);
        verify(warehouseManagerRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(warehouseManagerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            warehouseManagerService.delete(999L);
        });

        assertTrue(exception.getMessage().contains("WarehouseManager"));
        verify(warehouseManagerRepository, times(1)).findById(999L);
        verify(refreshTokenRepository, never()).deleteAllByUserId(anyLong());
        verify(warehouseManagerRepository, never()).deleteById(any());
    }

    // ==================== DEACTIVATE TESTS ====================

    @Test
    void testDeactivate_Success() {
        // Arrange
        when(warehouseManagerRepository.findById(1L)).thenReturn(Optional.of(warehouseManager));
        when(warehouseManagerRepository.save(any(WarehouseManager.class)))
                .thenReturn(warehouseManager);
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        // Act
        WarehouseManagerResponseDto result = warehouseManagerService.deactivate(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Warehouse Manager désactivé", result.getMessage());

        verify(warehouseManagerRepository, times(1)).findById(1L);
        verify(warehouseManagerRepository, times(1)).save(any(WarehouseManager.class));
    }

    @Test
    void testDeactivate_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(warehouseManagerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            warehouseManagerService.deactivate(999L);
        });

        assertTrue(exception.getMessage().contains("WarehouseManager"));
        verify(warehouseManagerRepository, times(1)).findById(999L);
        verify(warehouseManagerRepository, never()).save(any());
    }

    @Test
    void testGetAll_Success() {
        when(warehouseManagerRepository.findAll()).thenReturn(Arrays.asList(warehouseManager));
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        List<WarehouseManagerResponseDto> result = warehouseManagerService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(warehouseManagerRepository, times(1)).findAll();
    }

    @Test
    void testActivate_Success() {
        warehouseManager.setActive(false);
        when(warehouseManagerRepository.findById(1L)).thenReturn(Optional.of(warehouseManager));
        when(warehouseManagerRepository.save(any(WarehouseManager.class))).thenReturn(warehouseManager);
        when(warehouseManagerMapper.toDto(warehouseManager)).thenReturn(responseDto);

        WarehouseManagerResponseDto result = warehouseManagerService.activate(1L);

        assertNotNull(result);
        assertEquals("Warehouse Manager activé", result.getMessage());
        assertTrue(warehouseManager.getActive());
        verify(warehouseManagerRepository, times(1)).findById(1L);
        verify(warehouseManagerRepository, times(1)).save(warehouseManager);
    }

    @Test
    void testActivate_NotFound_ThrowsResourceNotFoundException() {
        when(warehouseManagerRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            warehouseManagerService.activate(999L);
        });

        assertTrue(exception.getMessage().contains("WarehouseManager"));
        verify(warehouseManagerRepository, times(1)).findById(999L);
        verify(warehouseManagerRepository, never()).save(any());
    }
}