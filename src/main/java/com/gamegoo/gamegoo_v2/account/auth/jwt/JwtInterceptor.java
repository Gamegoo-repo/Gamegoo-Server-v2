package com.gamegoo.gamegoo_v2.account.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.exception.JwtAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // OPTIONS 메서드이면 토큰 검증하지 않음
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        try {
            // request에서 access token 추출
            String accessToken = jwtProvider.resolveToken(request);

            // access token 값 검증
            jwtProvider.validateToken(accessToken);

            // access token에서 memberId 추출
            Long memberId = jwtProvider.getMemberId(accessToken);

            // request 객체에 값 저장
            request.setAttribute("memberId", memberId);
        } catch (JwtAuthException exception) {
            sendErrorResponse(response, exception, request.getRequestURI());
            return false;
        }

        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, JwtAuthException exception,
                                   String requestUrl) throws IOException {
        // 응답 Content-Type 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(exception.getStatus().value());

        // 에러 응답 생성
        ApiResponse<Object> errorResponse = ApiResponse.builder()
                .status(exception.getStatus())
                .code(exception.getCode())
                .message(exception.getMessage())
                .build();

        // JSON 직렬화 및 응답 작성
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}
