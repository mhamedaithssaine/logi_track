package org.example.logistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("WAREHOUSE_MANAGER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WarehouseManager extends User {
    @Column(nullable = true)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = true)
    private Warehouse warehouse;
}
