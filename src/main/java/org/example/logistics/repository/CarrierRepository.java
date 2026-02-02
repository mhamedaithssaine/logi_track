package org.example.logistics.repository;

import org.example.logistics.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {

    List<Carrier> findByActiveTrueOrderByNameAsc();

    List<Carrier> findAllByOrderByNameAsc();

    Optional<Carrier> findByCode(String code);

    boolean existsByCode(String code);
}
