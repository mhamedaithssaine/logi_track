package org.example.logistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("CLIENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Client  extends User{

    @Column(nullable = false)
    private String phone;
    @Column
    private String address;
}
