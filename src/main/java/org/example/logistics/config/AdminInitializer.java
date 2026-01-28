package org.example.logistics.config;

import lombok.RequiredArgsConstructor;
import org.example.logistics.entity.User;
import org.example.logistics.entity.Enum.Role;
import org.example.logistics.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner createAdmin(UserRepository userRepository) {
        return args -> {

            if (userRepository.existsByEmail("admin@gmail.com")) {
                System.out.println("Admin already exists");
                return;
            }

            User admin = User.builder()
                    .name("Super Admin")
                    .email("admin@gmail.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();

            userRepository.save(admin);

            System.out.println("ADMIN CREATED SUCCESSFULLY");
        };
    }
}
