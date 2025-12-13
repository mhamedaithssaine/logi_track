package org.example.logistics.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logistics.entity.Enum.Role;
import org.springframework.data.annotation.Persistent;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCreateDto {
    @NotBlank(message = "le nome de client est obligatoire")
    private String name;
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Format de téléphone marocain invalide")
    private String phone;
    private String address;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Au moins 6 caractères")
    private String password;
}