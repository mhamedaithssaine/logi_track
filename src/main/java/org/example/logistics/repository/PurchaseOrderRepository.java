package org.example.logistics.repository;

import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
    Optional<PurchaseOrder> findByIdAndStatus(Long id, Status status);
}
