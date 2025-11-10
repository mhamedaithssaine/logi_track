package org.example.logistics.repository;

import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {



    @Query("SELECT COUNT(sol) FROM SalesOrderLine sol " +
            "WHERE sol.product.id = :productId " +
            "AND sol.salesOrder.status IN :statuses")
    long countActiveOrdersForProduct(@Param("productId") Long productId, @Param("statuses") List<Status> statuses);




}
