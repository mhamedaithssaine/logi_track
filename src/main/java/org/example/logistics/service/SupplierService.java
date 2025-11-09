package org.example.logistics.service;

import org.example.logistics.dto.supplier.SupplierCreateDto;
import org.example.logistics.dto.supplier.SupplierResponseDto;
import org.example.logistics.dto.supplier.SupplierUpdateDto;
import org.example.logistics.entity.Supplier;
import org.example.logistics.entity.Warehouse;
import org.example.logistics.mapper.SupplierMapper;
import org.example.logistics.repository.SupplierRepository;
import org.example.logistics.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private SupplierMapper supplierMapper;

    // Create
    @Transactional
    public SupplierResponseDto createSupplier(SupplierCreateDto dto) {
        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) {
            throw new RuntimeException("Entrepôt non trouvé : ID " + dto.getWarehouseId());
        }

        Supplier supplier = supplierMapper.toEntity(dto);
        if (dto.getWarehouseId() != null) {
            supplier.setWarehouse(warehouseRepository.findById(dto.getWarehouseId()).get());
        }
        Supplier saved = supplierRepository.save(supplier);

        return SupplierResponseDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .contact(saved.getContact())
                .warehouseId(saved.getWarehouse() != null ? saved.getWarehouse().getId() : null)
                .warehouseName(saved.getWarehouse() != null ? saved.getWarehouse().getName() : null)
                .message("Fournisseur créé avec succès")
                .build();
    }

    // Read All
    @Transactional(readOnly = true)
    public List<SupplierResponseDto> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return suppliers.stream()
                .map(supplierMapper::toDto)
                .collect(Collectors.toList());
    }

    // Read by ID
    @Transactional(readOnly = true)
    public SupplierResponseDto getSupplierById(Long id) {
        Optional<Supplier> opt = supplierRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Fournisseur non trouvé : ID " + id);
        }

        return supplierMapper.toDto(opt.get());
    }

    // Update
    @Transactional
    public SupplierResponseDto updateSupplier(Long id, SupplierUpdateDto dto) {
        Optional<Supplier> opt = supplierRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Fournisseur non trouvé : ID " + id);
        }

        Supplier supplier = opt.get();
        supplierMapper.toEntityFromUpdate(dto, supplier);

        if (dto.getWarehouseId() != null && !warehouseRepository.existsById(dto.getWarehouseId())) {
            throw new RuntimeException("Entrepôt non trouvé : ID " + dto.getWarehouseId());
        }
        if (dto.getWarehouseId() != null) {
            supplier.setWarehouse(warehouseRepository.findById(dto.getWarehouseId()).get());
        }

        Supplier saved = supplierRepository.save(supplier);

        return SupplierResponseDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .contact(saved.getContact())
                .warehouseId(saved.getWarehouse() != null ? saved.getWarehouse().getId() : null)
                .warehouseName(saved.getWarehouse() != null ? saved.getWarehouse().getName() : null)
                .message("Fournisseur mis à jour avec succès")
                .build();
    }

    // Delete
    @Transactional
    public String deleteSupplier(Long id) {
        Optional<Supplier> opt = supplierRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Fournisseur non trouvé : ID " + id);
        }

        supplierRepository.delete(opt.get());
        return "Fournisseur supprimé avec succès";
    }
}