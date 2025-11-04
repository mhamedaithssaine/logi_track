package org.example.logistics.repository;

import org.example.logistics.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client,Long> {
    Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Client> findByEmailAndActiveTrue(String email);


}
