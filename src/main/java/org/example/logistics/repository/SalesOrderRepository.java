package org.example.logistics.repository;

import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder,Long> {
    List<SalesOrder> findByClientId(Long clientId);
    Optional<SalesOrder> findByIdAndStatus(Long id , Status status);


    @Query("SELECT s FROM SalesOrder s WHERE s.id = :id " +
            "AND s.status IN ('CREATED', 'RESERVED', 'PARTIAL_RESERVED')")
    Optional<SalesOrder> findCancelableOrderById(@Param("id") Long id);


    @Query("SELECT SUM(line.quantity) FROM SalesOrder s JOIN s.lines line WHERE s.id = :id")
    Integer totalQtyReserved(@Param("id") Long id);


    @Query("SELECT s FROM SalesOrder s WHERE s.status = 'CANCELED' " +
            "AND s.canceledAt BETWEEN :startDate AND :endDate")
    List<SalesOrder> findCanceledOrdersBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    long countByStatus(Status status);
}
