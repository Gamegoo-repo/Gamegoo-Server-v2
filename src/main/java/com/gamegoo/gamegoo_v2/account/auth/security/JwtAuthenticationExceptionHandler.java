package com.gamegoo.gamegoo_v2.account.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.exception.JwtAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class JwtAuthenticationExceptionHandler extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtAuthException authException) {
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());

            PrintWriter writer = response.getWriter();
            ApiResponse<Object> apiResponse = ApiResponse.builder()
                    .code(authException.getCode())
                    .message(authException.getMessage())
                    .data(null)
                    .status(authException.getStatus())
                    .build();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            writer.write(jsonResponse);
            writer.flush();
            writer.close();
        }
    }

}
