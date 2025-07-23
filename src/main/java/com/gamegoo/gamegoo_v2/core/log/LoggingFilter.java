package com.gamegoo.gamegoo_v2.core.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private final LogUtil logUtil;

    private static final Set<String> EXCLUDED_LOGGING_PATHS = Set.of(
            "/healthcheck",
            "/actuator/prometheus"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (EXCLUDED_LOGGING_PATHS.contains(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청 로그 출력
        logUtil.apiRequest(request);
        long startTime = System.currentTimeMillis();

        // 다음 로직 실행
        filterChain.doFilter(request, response);

        long executionTime = System.currentTimeMillis() - startTime;

        // 응답 로그 출력
        logUtil.apiResponse(request, response, executionTime);
    }

}
