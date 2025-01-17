package com.gamegoo.gamegoo_v2.account.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(403);
        PrintWriter writer = response.getWriter();

        // 실패 응답
        ApiResponse<Object> apiResponse =
                ApiResponse.builder()
                        .code(ErrorCode._FORBIDDEN.getCode())
                        .message(ErrorCode._FORBIDDEN.getMessage())
                        .data(null)
                        .status(ErrorCode._FORBIDDEN.getStatus())
                        .build();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            writer.write(jsonResponse);
        } catch (NullPointerException e) {
            log.error("응답 메시지 작성 에러", e);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

}
