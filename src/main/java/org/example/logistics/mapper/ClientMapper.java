package org.example.logistics.mapper;


import org.example.logistics.dto.client.ClientRegisterDto;
import org.example.logistics.dto.client.ClientResponseDto;
import org.example.logistics.entity.Client;
import org.example.logistics.entity.Enum.Role;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public Client toEntity(ClientRegisterDto dto){
        if(dto == null){
            return null;
        }

        return Client.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .passwordHash(null)
                .role(Role.CLIENT)
                .active(true)
                .build();
    }


    public ClientResponseDto toDto(Client entity) {
        if (entity == null) {
            return null;
        }

        return ClientResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .role(entity.getRole())
                .active(entity.getActive())
                .build();
    }


    public ClientResponseDto toLoginDto(Client entity) {
        if (entity == null) {
            return null;
        }

        ClientResponseDto dto = toDto(entity);
        String token = generateToken(entity);
        dto.setToken(token);

        return dto;
    }

    public void updateEntityFromDto(ClientRegisterDto dto, Client entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
    }

    private String generateToken(Client entity) {
        return entity.getEmail() + "_" + System.currentTimeMillis();
    }

}