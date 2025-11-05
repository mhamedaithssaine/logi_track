package org.example.logistics.repository;

import org.example.logistics.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder,Long> {
    List<SalesOrder> findByClientId(Long clientId);
}
