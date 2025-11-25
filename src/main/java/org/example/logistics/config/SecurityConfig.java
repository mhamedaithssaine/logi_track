package org.example.logistics.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Désactivé pour API REST
                .authorizeHttpRequests(auth -> auth

                        // ROUTES PUBLIQUES (sans authentification)
                        .requestMatchers("/api/clients/register").permitAll()
                        .requestMatchers("/api/clients/login").permitAll()
                        .requestMatchers("/api/warehouse-managers/register").permitAll()
                        .requestMatchers("/api/warehouse-managers/login").permitAll()


                        // ROUTES PRODUCTS (ADMIN + WAREHOUSE_MANAGER)

                        .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")


                        // ROUTES SUPPLIERS (ADMIN + WAREHOUSE_MANAGER)
                        .requestMatchers("/api/suppliers/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")


                        // ROUTES WAREHOUSES (ADMIN + WAREHOUSE_MANAGER)
                        .requestMatchers("/api/warehouses/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")


                        // ROUTES INVENTORY (WAREHOUSE_MANAGER uniquement)
                        .requestMatchers("/api/inventory/**").hasRole("WAREHOUSE_MANAGER")


                        // ROUTES PURCHASE ORDERS (ADMIN + WAREHOUSE_MANAGER)
                        .requestMatchers("/api/purchase-orders/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")


                        // ROUTES WAREHOUSE MANAGERS (WAREHOUSE_MANAGER uniquement)
                        .requestMatchers("/api/warehouse-managers/**").hasRole("WAREHOUSE_MANAGER")


                        // ROUTES ORDERS (CLIENT peut créer/voir, WAREHOUSE_MANAGER peut gérer)
                        .requestMatchers("/api/orders/**").hasAnyRole("CLIENT", "WAREHOUSE_MANAGER")


                        // ROUTES SHIPMENTS (WAREHOUSE_MANAGER uniquement)

                        .requestMatchers("/api/shipments/**").hasRole("WAREHOUSE_MANAGER")


                        // ROUTES CLIENTS (CLIENT + ADMIN)
                        .requestMatchers("/api/clients/**").hasAnyRole("CLIENT", "ADMIN")


                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Utilisateur ADMIN
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        // Utilisateur WAREHOUSE_MANAGER
        UserDetails warehouseManager = User.builder()
                .username("warehouse")
                .password(passwordEncoder().encode("warehouse123"))
                .roles("WAREHOUSE_MANAGER")
                .build();

        // Utilisateur CLIENT
        UserDetails client = User.builder()
                .username("client")
                .password(passwordEncoder().encode("client123"))
                .roles("CLIENT")
                .build();

        return new InMemoryUserDetailsManager(admin, warehouseManager, client);
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}