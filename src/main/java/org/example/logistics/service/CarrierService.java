package org.example.logistics.service;

import org.example.logistics.dto.carrier.CarrierCreateDto;
import org.example.logistics.dto.carrier.CarrierResponseDto;
import org.example.logistics.dto.carrier.CarrierUpdateDto;
import org.example.logistics.entity.Carrier;
import org.example.logistics.exception.BadRequestException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.repository.CarrierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarrierService {

    @Autowired
    private CarrierRepository carrierRepository;

    public List<CarrierResponseDto> findAll(boolean activeOnly) {
        List<Carrier> list = activeOnly
                ? carrierRepository.findByActiveTrueOrderByNameAsc()
                : carrierRepository.findAllByOrderByNameAsc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public CarrierResponseDto findById(Long id) {
        Carrier c = carrierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("Transporteur", id));
        return toDto(c);
    }

    @Transactional
    public CarrierResponseDto create(CarrierCreateDto dto) {
        if (carrierRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Un transporteur avec le code " + dto.getCode() + " existe déjà.");
        }
        Carrier c = Carrier.builder()
                .code(dto.getCode().trim().toUpperCase())
                .name(dto.getName().trim())
                .active(dto.isActive())
                .build();
        return toDto(carrierRepository.save(c));
    }

    @Transactional
    public CarrierResponseDto update(Long id, CarrierUpdateDto dto) {
        Carrier c = carrierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.withId("Transporteur", id));
        c.setName(dto.getName().trim());
        c.setActive(dto.isActive());
        return toDto(carrierRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        if (!carrierRepository.existsById(id)) {
            throw ResourceNotFoundException.withId("Transporteur", id);
        }
        carrierRepository.deleteById(id);
    }

    private CarrierResponseDto toDto(Carrier c) {
        return CarrierResponseDto.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .active(c.isActive())
                .build();
    }
}
