package org.example.logistics.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logistics.entity.Enum.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientUpdateDto {
    @NotBlank(message = "Name requis")
    private String name;

    @Email(message = "Email invalide")
    @NotBlank(message = "Email requis")
    private String email;
    @Size(min = 6, max = 255, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    private String phone;

    private String address;
}