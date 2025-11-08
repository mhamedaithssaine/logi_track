package org.example.logistics.repository;

import org.example.logistics.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse,Long> {
    Optional<Warehouse> findByCode(String code);
    boolean existsByCode(String code);
}
