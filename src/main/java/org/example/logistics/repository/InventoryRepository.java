package org.example.logistics.repository;

import org.example.logistics.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId );
    List<Inventory> findByProductId(Long productId);

}
