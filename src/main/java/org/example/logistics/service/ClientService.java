package org.example.logistics.service;

import lombok.RequiredArgsConstructor;
import org.example.logistics.dto.client.*;
import org.example.logistics.entity.Client;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.ClientMapper;
import org.example.logistics.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;

    // Register/Create
    @Transactional
    public ClientResponseDto register(ClientRegisterDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email déjà utilisé : " + dto.getEmail());
        }

        Client client = clientMapper.toEntity(dto);
        // encode password if provided
        if (dto.getPassword() != null) {
            client.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        Client savedClient = clientRepository.save(client);
        ClientResponseDto response = clientMapper.toDto(savedClient);
        response.setMessage("Client créé avec succès");
        return response;
    }

    @Transactional(readOnly = true)
    public ClientResponseDto login(ClientLoginDto dto) {
        Client client = clientRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->  ResourceNotFoundException.withEmail("Client", dto.getEmail()));

        if (!passwordEncoder.matches(dto.getPassword(), client.getPasswordHash())) {
            throw new ConflictException("Mot de passe incorrect");
        }

        ClientResponseDto response = clientMapper.toDto(client);
        response.setMessage("Authentification réussie");
        // token generation omitted (not stored in entity). Set null or generate if needed.
        return response;
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() ->  ResourceNotFoundException.withId("Client", id));
        return clientMapper.toDto(client);
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() ->  ResourceNotFoundException.withEmail("Client", email));
        return clientMapper.toDto(client);
    }

    @Transactional
    public ClientResponseDto update(Long id, ClientRegisterDto dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() ->  ResourceNotFoundException.withId("Client", id));

        // Update only allowed fields (do not change email here)
        if (dto.getName() != null) client.setName(dto.getName());
        if (dto.getPhone() != null) client.setPhone(dto.getPhone());
        if (dto.getAddress() != null) client.setAddress(dto.getAddress());
        // If password provided, encode and update
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            client.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        Client saved = clientRepository.save(client);
        ClientResponseDto response = clientMapper.toDto(saved);
        response.setMessage("Client mis à jour");
        return response;
    }

    @Transactional
    public ClientResponseDto delete(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("Client", id));

        ClientResponseDto response = clientMapper.toDto(client);

        clientRepository.deleteById(id);
        response.setMessage("Client supprimé avec succès");
    return response;
    }

    @Transactional
    public ClientResponseDto deactivate(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() ->  ResourceNotFoundException.withId("Client", id));
        client.setActive(false);
        Client saved = clientRepository.save(client);
        ClientResponseDto response = clientMapper.toDto(saved);
        response.setMessage("Client désactivé");
        return response;
    }
}