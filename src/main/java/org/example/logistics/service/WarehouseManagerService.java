package org.example.logistics.service;

import lombok.RequiredArgsConstructor;
import org.example.logistics.dto.warehousemanager.WarehouseManagerLoginDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerRegisterDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerResponseDto;
import org.example.logistics.dto.warehousemanager.WarehouseManagerUpdateDto;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.entity.WarehouseManager;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.mapper.WarehouseManagerMapper;
import org.example.logistics.repository.WarehouseManagerRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseManagerService {

    @Autowired
    private  WarehouseManagerRepository warehouseManagerRepository;
    @Autowired
    private  WarehouseRepository warehouseRepository;
    @Autowired
    private  WarehouseManagerMapper warehouseManagerMapper;
    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Transactional
    public WarehouseManagerResponseDto register(WarehouseManagerRegisterDto dto) {
        if (warehouseManagerRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email déjà utilisé : " + dto.getEmail());
        }

        // Vérifier que le warehouse existe
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> ResourceNotFoundException.withId("Warehouse", dto.getWarehouseId()));

        WarehouseManager manager = warehouseManagerMapper.toEntity(dto);

        // Encoder le mot de passe
        if (dto.getPassword() != null) {
            manager.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        // Associer le warehouse
        manager.setWarehouse(warehouse);

        WarehouseManager saved = warehouseManagerRepository.save(manager);
        WarehouseManagerResponseDto response = warehouseManagerMapper.toDto(saved);
        response.setMessage("Warehouse Manager créé avec succès");
        return response;
    }

    @Transactional(readOnly = true)
    public WarehouseManagerResponseDto login(WarehouseManagerLoginDto dto) {
        WarehouseManager manager = warehouseManagerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> ResourceNotFoundException.withEmail("WarehouseManager", dto.getEmail()));

        if (!passwordEncoder.matches(dto.getPassword(), manager.getPasswordHash())) {
            throw new ConflictException("Mot de passe incorrect");
        }

        WarehouseManagerResponseDto response = warehouseManagerMapper.toDto(manager);
        response.setMessage("Authentification réussie");
        return response;
    }

    @Transactional(readOnly = true)
    public WarehouseManagerResponseDto getById(Long id) {
        WarehouseManager manager = warehouseManagerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("WarehouseManager", id));
        return warehouseManagerMapper.toDto(manager);
    }

    @Transactional(readOnly = true)
    public WarehouseManagerResponseDto getByEmail(String email) {
        WarehouseManager manager = warehouseManagerRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.withEmail("WarehouseManager", email));
        return warehouseManagerMapper.toDto(manager);
    }

    @Transactional
    public WarehouseManagerResponseDto update(Long id, WarehouseManagerUpdateDto dto) {
        WarehouseManager manager = warehouseManagerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("WarehouseManager", id));

        if (dto.getName() != null) manager.setName(dto.getName());
        if (dto.getPhone() != null) manager.setPhone(dto.getPhone());

        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                    .orElseThrow(() -> ResourceNotFoundException.withId("Warehouse", dto.getWarehouseId()));
            manager.setWarehouse(warehouse);
        }

        WarehouseManager saved = warehouseManagerRepository.save(manager);
        WarehouseManagerResponseDto response = warehouseManagerMapper.toDto(saved);
        response.setMessage("Warehouse Manager mis à jour");
        return response;
    }

    @Transactional
    public WarehouseManagerResponseDto delete(Long id) {
        WarehouseManager manager = warehouseManagerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("WarehouseManager", id));

        WarehouseManagerResponseDto response = warehouseManagerMapper.toDto(manager);

        warehouseManagerRepository.deleteById(id);

        response.setMessage("Warehouse Manager supprimé avec succès");
        return response;
    }

    @Transactional
    public WarehouseManagerResponseDto deactivate(Long id) {
        WarehouseManager manager = warehouseManagerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("WarehouseManager", id));

        manager.setActive(false);
        WarehouseManager saved = warehouseManagerRepository.save(manager);

        WarehouseManagerResponseDto response = warehouseManagerMapper.toDto(saved);
        response.setMessage("Warehouse Manager désactivé");
        return response;
    }
}