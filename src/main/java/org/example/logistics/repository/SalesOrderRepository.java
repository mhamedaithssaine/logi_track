package org.example.logistics.repository;

import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder,Long> {
    List<SalesOrder> findByClientId(Long clientId);
    Optional<SalesOrder> findByIdAndStatus(Long id , Status status);
}
