package org.example.logistics.service;

import org.example.logistics.dto.client.*;
import org.example.logistics.entity.Client;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.ClientMapper;
import org.example.logistics.repository.ClientRepository;
import org.example.logistics.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private ClientMapper clientMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getAll() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClientResponseDto create(ClientCreateDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email déjà utilisé : " + dto.getEmail());
        }
        Client client = clientMapper.toEntityFromCreate(dto);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            client.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        Client saved = clientRepository.save(client);
        ClientResponseDto response = clientMapper.toDto(saved);
        response.setMessage("Client créé avec succès");
        return response;
    }

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
    public ClientResponseDto update(Long id, ClientUpdateDto dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("Client", id));

        clientMapper.updateEntityFromDto(dto, client);
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
        response.setMessage("Client supprimé avec succès");

        refreshTokenRepository.deleteAllByUserId(id);
        clientRepository.deleteById(id);
        return response;
    }

    @Transactional
    public ClientResponseDto deactivate(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("Client", id));
        client.setActive(false);
        Client saved = clientRepository.save(client);
        ClientResponseDto response = clientMapper.toDto(saved);
        response.setMessage("Client désactivé");
        return response;
    }

    @Transactional
    public ClientResponseDto activate(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("Client", id));
        client.setActive(true);
        Client saved = clientRepository.save(client);
        ClientResponseDto response = ClientResponseDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .address(saved.getAddress())
                .role(saved.getRole() != null ? saved.getRole().name() : "CLIENT")
                .active(Boolean.TRUE.equals(saved.getActive()))
                .message("Client activé")
                .build();
        return response;
    }
}