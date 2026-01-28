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
import org.example.logistics.repository.RefreshTokenRepository;
import org.example.logistics.repository.WarehouseManagerRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseManagerService {

    @Autowired
    private WarehouseManagerRepository warehouseManagerRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private WarehouseManagerMapper warehouseManagerMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<WarehouseManagerResponseDto> getAll() {
        return warehouseManagerRepository.findAll().stream()
                .map(warehouseManagerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public WarehouseManagerResponseDto register(WarehouseManagerRegisterDto dto) {
        if (warehouseManagerRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email déjà utilisé : " + dto.getEmail());
        }

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> ResourceNotFoundException.withId("Warehouse", dto.getWarehouseId()));

        WarehouseManager manager = warehouseManagerMapper.toEntity(dto);

        if (dto.getPassword() != null) {
            manager.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

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
        response.setMessage("Warehouse Manager supprimé avec succès");

        refreshTokenRepository.deleteAllByUserId(id);
        warehouseManagerRepository.deleteById(id);
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

    @Transactional
    public WarehouseManagerResponseDto activate(Long id) {
        WarehouseManager manager = warehouseManagerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("WarehouseManager", id));
        manager.setActive(true);
        WarehouseManager saved = warehouseManagerRepository.save(manager);
        WarehouseManagerResponseDto response = warehouseManagerMapper.toDto(saved);
        response.setMessage("Warehouse Manager activé");
        return response;
    }
}