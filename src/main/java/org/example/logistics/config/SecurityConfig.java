package org.example.logistics.config;

import org.example.logistics.security.CustomUserDetailsService;
import org.example.logistics.security.JwtAuthenticationFilter;
import org.example.logistics.security.SecurityExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private SecurityExceptionHandler securityExceptionHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/clients/register").permitAll()
                        .requestMatchers("/api/warehouse-managers/register").permitAll()
                        .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers("/api/suppliers/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers("/api/warehouses/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers("/api/inventory/**").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers("/api/purchase-orders/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers("/api/warehouse-managers/**").hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
                        .requestMatchers("/api/warehouse-manager/{id}/deactivate").hasRole("ADMIN")
                        .requestMatchers("/api/warehouse-manager/{id}/delete").hasRole("ADMIN")
                        .requestMatchers("/api/orders/**").hasAnyRole("CLIENT", "WAREHOUSE_MANAGER")
                        .requestMatchers("/api/shipments/**").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers("/api/clients/**").hasAnyRole("CLIENT", "ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(securityExceptionHandler)
                        .authenticationEntryPoint(securityExceptionHandler)
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}