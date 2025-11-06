package org.example.logistics.repository;

import org.example.logistics.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> {
    Optional<Shipment> findBySalesOrderId(Long salesOrderId);
}
