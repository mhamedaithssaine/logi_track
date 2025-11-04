package org.example.logistics.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logistics.entity.Enum.Role;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Role role;
    private Boolean active;
    private String token;
}
