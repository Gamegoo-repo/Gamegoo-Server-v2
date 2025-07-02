package com.gamegoo.gamegoo_v2.account.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

import static com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode._INTERNAL_SERVER_ERROR;


@Slf4j
@RequiredArgsConstructor
@Component
public class EntryPointUnauthorizedHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {

        var errorCode = _INTERNAL_SERVER_ERROR;

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .data(null)
                .status(errorCode.getStatus())
                .build();

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        try (PrintWriter writer = response.getWriter()) {
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            writer.write(jsonResponse);
            writer.flush();
        } catch (Exception e) {
            log.error("Security Filter EntryPoint 응답 메시지 작성 에러", e);
        }
    }

}
