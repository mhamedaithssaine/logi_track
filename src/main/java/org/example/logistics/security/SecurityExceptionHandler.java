package org.example.logistics.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.logistics.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SecurityExceptionHandler
        implements AccessDeniedHandler, AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    //  403 Forbidden
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Accès interdit")
                .message("Vous n'avez pas les permissions nécessaires")
                .path(request.getRequestURI())
                .build();

        writeResponse(response, HttpStatus.FORBIDDEN, error);
    }

    // 401 Unauthorized
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) throws IOException {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Non authentifié")
                .message("Token invalide, expiré ou manquant")
                .path(request.getRequestURI())
                .build();

        writeResponse(response, HttpStatus.UNAUTHORIZED, error);
    }

    private void writeResponse(
            HttpServletResponse response,
            HttpStatus status,
            ErrorResponse error
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), error);
    }
}
