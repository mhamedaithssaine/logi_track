package org.example.logistics.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCreateDto {

    @NotBlank(message = "Le nom est requis")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    private String email;

    @Size(max = 20, message = "Le téléphone ne doit pas dépasser 20 caractères")
    private String phone;

    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String address;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, max = 255, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;
}