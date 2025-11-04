package org.example.logistics.service;

import org.example.logistics.dto.client.ClientLoginDto;
import org.example.logistics.dto.client.ClientRegisterDto;
import org.example.logistics.dto.client.ClientResponseDto;
import org.example.logistics.entity.Client;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.exception.UnauthorizedException;
import org.example.logistics.mapper.ClientMapper;
import org.example.logistics.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ClientMapper clientMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //save
    public ClientResponseDto register(ClientRegisterDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email deja utilise: " + dto.getEmail());
        }

        Client client = clientMapper.toEntity(dto);
        client.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        Client savedClient = clientRepository.save(client);
        return clientMapper.toDto(savedClient);
    }


    //login
    public ClientResponseDto login(ClientLoginDto dto) {
        Client client = clientRepository.findByEmailAndActiveTrue(dto.getEmail())
                .orElseThrow(() ->
                        ResourceNotFoundException.withEmail("Client", dto.getEmail())
                );

        if (!passwordEncoder.matches(dto.getPassword(), client.getPasswordHash())) {
            throw new UnauthorizedException("Mot de passe incorrect");
        }

        return clientMapper.toLoginDto(client);
    }

    //get by id
    @Transactional(readOnly = true)
    public ClientResponseDto getById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.withId("Client", id)
                );
        return clientMapper.toDto(client);
    }


    //get by email
    @Transactional(readOnly = true)
    public ClientResponseDto getByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() ->
                        ResourceNotFoundException.withEmail("Client", email)
                );
        return clientMapper.toDto(client);
    }

    //update
    public ClientResponseDto update(Long id, ClientRegisterDto dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.withId("Client", id)
                );

        if (!client.getEmail().equals(dto.getEmail()) &&
                clientRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email deja utilise: " + dto.getEmail());
        }

        clientMapper.updateEntityFromDto(dto, client);
        Client updatedClient = clientRepository.save(client);

        return clientMapper.toDto(updatedClient);
    }

    // delete
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw ResourceNotFoundException.withId("Client", id);
        }
        clientRepository.deleteById(id);
    }

    // bane account
    public ClientResponseDto deactivate(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.withId("Client", id)
                );

        client.setActive(false);
        Client deactivatedClient = clientRepository.save(client);

        return clientMapper.toDto(deactivatedClient);
    }

}
