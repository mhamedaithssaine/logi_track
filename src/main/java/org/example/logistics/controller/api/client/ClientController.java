package org.example.logistics.controller.api.client;

import jakarta.validation.Valid;
import org.example.logistics.dto.client.ClientCreateDto;
import org.example.logistics.dto.client.ClientLoginDto;
import org.example.logistics.dto.client.ClientRegisterDto;
import org.example.logistics.dto.client.ClientResponseDto;
import org.example.logistics.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;


    @PostMapping("/register")
    public ResponseEntity<ClientResponseDto> register(@Valid @RequestBody ClientRegisterDto dto) {
        ClientResponseDto response = clientService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<ClientResponseDto> login(@Valid @RequestBody ClientLoginDto dto) {
        ClientResponseDto response = clientService.login(dto);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDto> getById(@PathVariable Long id) {
        ClientResponseDto response = clientService.getById(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<ClientResponseDto> getByEmail(@PathVariable String email) {
        ClientResponseDto response = clientService.getByEmail(email);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRegisterDto dto) {
        ClientResponseDto response = clientService.update(id, dto);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ClientResponseDto> delete(@PathVariable Long id) {
        ClientResponseDto response = clientService.delete(id);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ClientResponseDto> deactivate(@PathVariable Long id) {
        ClientResponseDto response = clientService.deactivate(id);
        return ResponseEntity.ok(response);
    }
}