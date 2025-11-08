package org.example.logistics.service;

import org.example.logistics.dto.warehouse.WarehouseCreateDto;
import org.example.logistics.dto.warehouse.WarehouseResponseDto;
import org.example.logistics.dto.warehouse.WarehouseUpdateDto;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.mapper.WarehouseMapper;
import org.example.logistics.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseMapper warehouseMapper;

    // Create
    @Transactional
    public WarehouseResponseDto createWarehouse(WarehouseCreateDto dto) {
        if (warehouseRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Code entrepôt existe déjà");
        }

        Warehouse warehouse = warehouseMapper.toEntity(dto);
        Warehouse saved = warehouseRepository.save(warehouse);

        return WarehouseResponseDto.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .name(saved.getName())
                .message("Entrepôt créé avec succès")
                .build();
    }

    // Read All
    @Transactional(readOnly = true)
    public List<WarehouseResponseDto> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        return warehouses.stream()
                .map(warehouseMapper::toDto)
                .collect(Collectors.toList());
    }

    // Read by Code
    @Transactional(readOnly = true)
    public WarehouseResponseDto getWarehouseByCode(String code) {
        Optional<Warehouse> opt = warehouseRepository.findByCode(code);
        if (opt.isEmpty()) {
            throw new RuntimeException("Entrepôt non trouvé pour code : " + code);
        }

        return warehouseMapper.toDto(opt.get());
    }


    // Update
    @Transactional
    public WarehouseResponseDto updateWarehouse(String code, WarehouseUpdateDto dto) {
        Optional<Warehouse> opt = warehouseRepository.findByCode(code);
        if (opt.isEmpty()) {
            throw new RuntimeException("Entrepôt non trouvé pour code : " + code);
        }

        Warehouse warehouse = opt.get();
        warehouseMapper.toEntityFromUpdate(dto, warehouse);
        Warehouse saved = warehouseRepository.save(warehouse);

        return WarehouseResponseDto.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .name(saved.getName())
                .message("Entrepôt mis à jour avec succès")
                .build();
    }

    // Delete
    @Transactional
    public String deleteWarehouse(String code) {
        Optional<Warehouse> opt = warehouseRepository.findByCode(code);
        if (opt.isEmpty()) {
            throw new RuntimeException("Entrepôt non trouvé pour code : " + code);
        }

        warehouseRepository.delete(opt.get());
        return "Entrepôt supprimé avec succès";
    }

}
