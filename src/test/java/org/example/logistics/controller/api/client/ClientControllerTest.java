package org.example.logistics.controller.api.client;

import org.example.logistics.dto.client.ClientLoginDto;
import org.example.logistics.dto.client.ClientRegisterDto;
import org.example.logistics.dto.client.ClientResponseDto;
import org.example.logistics.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Client Controller Tests")
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private ClientRegisterDto registerDto;
    private ClientLoginDto loginDto;
    private ClientResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        registerDto = ClientRegisterDto.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("SecurePassword123")
                .phone("0612345678")
                .address("123 Main Street, Paris")
                .build();

        loginDto = ClientLoginDto.builder()
                .email("john.doe@example.com")
                .password("SecurePassword123")
                .build();

        responseDto = ClientResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("0612345678")
                .address("123 Main Street, Paris")
                .role("CLIENT")
                .active(true)
                .message("Opération réussie")
                .build();
    }

    @Test
    @DisplayName("Should register client successfully")
    void testRegister_Success() {
        // Given
        when(clientService.register(any(ClientRegisterDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<ClientResponseDto> response = clientController.register(registerDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("john.doe@example.com", response.getBody().getEmail());
        assertEquals("CLIENT", response.getBody().getRole());
        assertTrue(response.getBody().getActive());

        verify(clientService, times(1)).register(any(ClientRegisterDto.class));
    }

    @Test
    @DisplayName("Should login successfully")
    void testLogin_Success() {
        // Given
        when(clientService.login(any(ClientLoginDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<ClientResponseDto> response = clientController.login(loginDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("john.doe@example.com", response.getBody().getEmail());

        verify(clientService, times(1)).login(any(ClientLoginDto.class));
    }

    @Test
    @DisplayName("Should get client by ID successfully")
    void testGetById_Success() {
        // Given
        when(clientService.getById(1L)).thenReturn(responseDto);

        // When
        ResponseEntity<ClientResponseDto> response = clientController.getById(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());

        verify(clientService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void testGetById_NotFound() {
        // Given
        when(clientService.getById(999L)).thenThrow(new RuntimeException("Client introuvable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clientController.getById(999L);
        });

        verify(clientService, times(1)).getById(999L);
    }

    @Test
    @DisplayName("Should get client by email successfully")
    void testGetByEmail_Success() {
        // Given
        when(clientService.getByEmail("john.doe@example.com")).thenReturn(responseDto);

        // When
        ResponseEntity<ClientResponseDto> response = clientController.getByEmail("john.doe@example.com");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe@example.com", response.getBody().getEmail());

        verify(clientService, times(1)).getByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should update client successfully")
    void testUpdate_Success() {
        // Given
        ClientRegisterDto updateDto = ClientRegisterDto.builder()
                .name("John Doe Updated")
                .email("john.updated@example.com")
                .password("NewPassword123")
                .phone("0698765432")
                .address("456 New Street")
                .build();

        ClientResponseDto updatedResponse = ClientResponseDto.builder()
                .id(1L)
                .name("John Doe Updated")
                .email("john.updated@example.com")
                .phone("0698765432")
                .address("456 New Street")
                .role("CLIENT")
                .active(true)
                .message("Client mis à jour")
                .build();

        when(clientService.update(eq(1L), any(ClientRegisterDto.class))).thenReturn(updatedResponse);

        // When
        ResponseEntity<ClientResponseDto> response = clientController.update(1L, updateDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe Updated", response.getBody().getName());
        assertEquals("john.updated@example.com", response.getBody().getEmail());

        verify(clientService, times(1)).update(eq(1L), any(ClientRegisterDto.class));
    }

    @Test
    @DisplayName("Should delete client successfully")
    void testDelete_Success() {
        // Given
        ClientResponseDto deletedResponse = ClientResponseDto.builder()
                .id(1L)
                .message("Client supprimé avec succès")
                .build();
        when(clientService.delete(1L)).thenReturn(deletedResponse);

        // When
        ResponseEntity<ClientResponseDto> response = clientController.delete(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(clientService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Should deactivate client successfully")
    void testDeactivate_Success() {
        // Given
        ClientResponseDto deactivatedResponse = ClientResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .role("CLIENT")
                .active(false)
                .message("Client désactivé")
                .build();
        when(clientService.deactivate(1L)).thenReturn(deactivatedResponse);

        // When
        ResponseEntity<ClientResponseDto> response = clientController.deactivate(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertFalse(response.getBody().getActive());

        verify(clientService, times(1)).deactivate(1L);
    }

    @Test
    @DisplayName("Should handle login with invalid credentials")
    void testLogin_InvalidCredentials() {
        // Given
        when(clientService.login(any(ClientLoginDto.class)))
                .thenThrow(new RuntimeException("Identifiants invalides"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clientController.login(loginDto);
        });

        verify(clientService, times(1)).login(any(ClientLoginDto.class));
    }

    @Test
    @DisplayName("Should handle duplicate email on register")
    void testRegister_DuplicateEmail() {
        // Given
        when(clientService.register(any(ClientRegisterDto.class)))
                .thenThrow(new RuntimeException("Email déjà utilisé"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clientController.register(registerDto);
        });

        verify(clientService, times(1)).register(any(ClientRegisterDto.class));
    }

    @Test
    @DisplayName("Should handle update with non-existent client")
    void testUpdate_NotFound() {
        // Given
        when(clientService.update(eq(999L), any(ClientRegisterDto.class)))
                .thenThrow(new RuntimeException("Client introuvable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clientController.update(999L, registerDto);
        });

        verify(clientService, times(1)).update(eq(999L), any(ClientRegisterDto.class));
    }

    @Test
    @DisplayName("Should handle delete with non-existent client")
    void testDelete_NotFound() {
        // Given
        when(clientService.delete(999L))
                .thenThrow(new RuntimeException("Client introuvable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clientController.delete(999L);
        });

        verify(clientService, times(1)).delete(999L);
    }

    @Test
    @DisplayName("Should handle deactivate with non-existent client")
    void testDeactivate_NotFound() {
        // Given
        when(clientService.deactivate(999L))
                .thenThrow(new RuntimeException("Client introuvable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clientController.deactivate(999L);
        });

        verify(clientService, times(1)).deactivate(999L);
    }

    @Test
    @DisplayName("Should verify all controller methods are called")
    void testControllerMethodsCalls() {
        // Given
        when(clientService.register(any(ClientRegisterDto.class))).thenReturn(responseDto);
        when(clientService.login(any(ClientLoginDto.class))).thenReturn(responseDto);
        when(clientService.getById(anyLong())).thenReturn(responseDto);
        when(clientService.getByEmail(anyString())).thenReturn(responseDto);
        when(clientService.update(anyLong(), any(ClientRegisterDto.class))).thenReturn(responseDto);
        when(clientService.delete(anyLong())).thenReturn(responseDto);
        when(clientService.deactivate(anyLong())).thenReturn(responseDto);

        // When
        clientController.register(registerDto);
        clientController.login(loginDto);
        clientController.getById(1L);
        clientController.getByEmail("john.doe@example.com");
        clientController.update(1L, registerDto);
        clientController.delete(1L);
        clientController.deactivate(1L);

        // Then
        verify(clientService, times(1)).register(any(ClientRegisterDto.class));
        verify(clientService, times(1)).login(any(ClientLoginDto.class));
        verify(clientService, times(1)).getById(1L);
        verify(clientService, times(1)).getByEmail("john.doe@example.com");
        verify(clientService, times(1)).update(anyLong(), any(ClientRegisterDto.class));
        verify(clientService, times(1)).delete(1L);
        verify(clientService, times(1)).deactivate(1L);
    }
}