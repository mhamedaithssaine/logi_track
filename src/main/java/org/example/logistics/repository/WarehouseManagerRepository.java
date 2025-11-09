package org.example.logistics.repository;

import org.example.logistics.entity.WarehouseManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseManagerRepository extends JpaRepository<WarehouseManager, Long> {
    boolean existsByEmail(String email);
    Optional<WarehouseManager> findByEmail(String email);
}