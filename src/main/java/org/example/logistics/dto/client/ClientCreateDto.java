package org.example.logistics.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logistics.entity.Enum.Role;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCreateDto {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String password;
}