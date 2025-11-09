package org.example.logistics.repository;

import org.example.logistics.entity.Enum.Status;
import org.example.logistics.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
    Optional<PurchaseOrder> findByIdAndStatus(Long id, Status status);


    @Query("SELECT p FROM PurchaseOrder p WHERE p.id = :id AND p.status = 'APPROVED'")
    Optional<PurchaseOrder> findCancelablePurchaseOrderById(@Param("id") Long id);



    @Query("SELECT p FROM PurchaseOrder p WHERE p.status = 'CANCELED' " +
            "AND p.canceledAt BETWEEN :startDate AND :endDate")
    List<PurchaseOrder> findCanceledPurchaseOrdersBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    long countByStatus(Status status);


    List<PurchaseOrder> findBySupplierId(Long supplierId);

}
