package org.example.logistics.service;

import org.example.logistics.dto.client.ClientLoginDto;
import org.example.logistics.dto.client.ClientRegisterDto;
import org.example.logistics.dto.client.ClientResponseDto;
import org.example.logistics.dto.client.ClientUpdateDto;
import org.example.logistics.entity.Client;
import org.example.logistics.entity.Enum.Role;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.ClientMapper;
import org.example.logistics.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Client Service Tests")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientService clientService;

    private ClientRegisterDto registerDto;
    private ClientUpdateDto updateDto;
    private ClientLoginDto loginDto;
    private Client client;
    private ClientResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        registerDto = ClientRegisterDto.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("Password123")
                .phone("0612345678")
                .address("123 Main St, Paris")
                .build();

        loginDto = ClientLoginDto.builder()
                .email("john.doe@example.com")
                .password("Password123")
                .build();

        client = Client.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .passwordHash("$2a$10$encodedPassword")
                .phone("0612345678")
                .address("123 Main St, Paris")
                .role(Role.CLIENT)
                .active(true)
                .build();

        responseDto = ClientResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("0612345678")
                .address("123 Main St, Paris")
                .role("CLIENT")
                .active(true)
                .build();

        updateDto = ClientUpdateDto.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("0612345678")
                .address("123 Main St, Paris")
                .build();
    }

    @Test
    @DisplayName("Should register client successfully")
    void testRegister_Success() {
        // Given
        when(clientRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
        when(clientMapper.toEntity(registerDto)).thenReturn(client);
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("$2a$10$encodedPassword");
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.register(registerDto);

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("Client créé avec succès", result.getMessage());

        verify(clientRepository, times(1)).existsByEmail(registerDto.getEmail());
        verify(passwordEncoder, times(1)).encode(registerDto.getPassword());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when email already exists")
    void testRegister_EmailExists() {
        // Given
        when(clientRepository.existsByEmail(registerDto.getEmail())).thenReturn(true);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            clientService.register(registerDto);
        });

        assertTrue(exception.getMessage().contains("Email déjà utilisé"));
        verify(clientRepository, times(1)).existsByEmail(registerDto.getEmail());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should register client without password")
    void testRegister_WithoutPassword() {
        // Given
        registerDto.setPassword(null);
        when(clientRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
        when(clientMapper.toEntity(registerDto)).thenReturn(client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.register(registerDto);

        // Then
        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should login successfully")
    void testLogin_Success() {
        // Given
        when(clientRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(loginDto.getPassword(), client.getPasswordHash())).thenReturn(true);
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.login(loginDto);

        // Then
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("Authentification réussie", result.getMessage());

        verify(clientRepository, times(1)).findByEmail(loginDto.getEmail());
        verify(passwordEncoder, times(1)).matches(loginDto.getPassword(), client.getPasswordHash());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when email not found during login")
    void testLogin_EmailNotFound() {
        // Given
        when(clientRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.login(loginDto);
        });

        verify(clientRepository, times(1)).findByEmail(loginDto.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw ConflictException when password is incorrect")
    void testLogin_IncorrectPassword() {
        // Given
        when(clientRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(loginDto.getPassword(), client.getPasswordHash())).thenReturn(false);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            clientService.login(loginDto);
        });

        assertTrue(exception.getMessage().contains("Mot de passe incorrect"));
        verify(passwordEncoder, times(1)).matches(loginDto.getPassword(), client.getPasswordHash());
    }

    @Test
    @DisplayName("Should get client by ID successfully")
    void testGetById_Success() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());

        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client not found by ID")
    void testGetById_NotFound() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.getById(999L);
        });

        verify(clientRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get client by email successfully")
    void testGetByEmail_Success() {
        // Given
        when(clientRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.getByEmail("john.doe@example.com");

        // Then
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());

        verify(clientRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client not found by email")
    void testGetByEmail_NotFound() {
        // Given
        when(clientRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.getByEmail("unknown@example.com");
        });

        verify(clientRepository, times(1)).findByEmail("unknown@example.com");
    }

    @Test
    @DisplayName("Should update client successfully")
    void testUpdate_Success() {
        // Given
        updateDto.setName("Jane Doe Updated");
        updateDto.setPhone("0698765432");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        doNothing().when(clientMapper).updateEntityFromDto(any(ClientUpdateDto.class), eq(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.update(1L, updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Client mis à jour", result.getMessage());

        verify(clientRepository, times(1)).findById(1L);
        verify(clientMapper, times(1)).updateEntityFromDto(any(ClientUpdateDto.class), eq(client));
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should update client with new password")
    void testUpdate_WithPassword() {
        // Given
        updateDto.setPassword("NewPassword456");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        doNothing().when(clientMapper).updateEntityFromDto(any(ClientUpdateDto.class), eq(client));
        when(passwordEncoder.encode("NewPassword456")).thenReturn("$2a$10$newEncodedPassword");
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.update(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("NewPassword456");
    }

    @Test
    @DisplayName("Should not update password if blank")
    void testUpdate_BlankPassword() {
        // Given
        updateDto.setPassword("   ");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        doNothing().when(clientMapper).updateEntityFromDto(any(ClientUpdateDto.class), eq(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.update(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent client")
    void testUpdate_NotFound() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.update(999L, updateDto);
        });

        verify(clientRepository, times(1)).findById(999L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should delete client successfully")
    void testDelete_Success() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(responseDto);
        doNothing().when(clientRepository).deleteById(1L);

        // When
        ClientResponseDto result = clientService.delete(1L);

        // Then
        assertNotNull(result);
        assertEquals("Client supprimé avec succès", result.getMessage());

        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent client")
    void testDelete_NotFound() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.delete(999L);
        });

        verify(clientRepository, times(1)).findById(999L);
        verify(clientRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should deactivate client successfully")
    void testDeactivate_Success() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.deactivate(1L);

        // Then
        assertNotNull(result);
        assertEquals("Client désactivé", result.getMessage());
        assertFalse(client.getActive());

        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deactivating non-existent client")
    void testDeactivate_NotFound() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.deactivate(999L);
        });

        verify(clientRepository, times(1)).findById(999L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should activate client successfully")
    void testActivate_Success() {
        // Given
        client.setActive(false);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // When
        ClientResponseDto result = clientService.activate(1L);

        // Then
        assertNotNull(result);
        assertEquals("Client activé", result.getMessage());
        assertTrue(client.getActive());
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());

        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when activating non-existent client")
    void testActivate_NotFound() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.activate(999L);
        });

        verify(clientRepository, times(1)).findById(999L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @ParameterizedTest
    @DisplayName("Should handle update with optional phone/address")
    @MethodSource("provideOptionalFieldScenarios")
    void testUpdate_OptionalFields(String phone, String address) {
        // Given
        updateDto.setPhone(phone);
        updateDto.setAddress(address);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        doNothing().when(clientMapper).updateEntityFromDto(any(ClientUpdateDto.class), eq(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(responseDto);

        // When
        ClientResponseDto result = clientService.update(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    private static Stream<Arguments> provideOptionalFieldScenarios() {
        return Stream.of(
                Arguments.of("0612345678", "Address"),
                Arguments.of(null, "Address"),
                Arguments.of("0612345678", null)
        );
    }
}