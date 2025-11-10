package org.example.logistics.repository;

import org.example.logistics.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {


    Optional<SalesOrderLine> findByProductId(Long id);


}
