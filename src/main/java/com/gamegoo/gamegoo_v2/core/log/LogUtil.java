package com.gamegoo.gamegoo_v2.core.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.gamegoo_v2.account.auth.security.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogUtil {

    private final ObjectMapper objectMapper;

    /**
     * api request 로그 출력
     *
     * @param request request 객체
     */
    public void apiRequest(HttpServletRequest request) {
        String requestId = MDC.get("requestId");
        String requestUrl = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = getClientIp(request);
        Long memberId = SecurityUtil.getCurrentMemberId();
        String params = getParamsAsString(request);
        String memberIdString = "Unauthenticated";

        if (memberId != null) {
            memberIdString = String.valueOf(memberId);
        }

        if (request.getParameterMap().isEmpty()) {
            log.info("[{}] [{}] {} | IP: {} | Member: {}", requestId, httpMethod, requestUrl, clientIp, memberIdString);
        } else {
            log.info("[{}] [{}] {} | IP: {} | Member: {} | Params: {}", requestId, httpMethod, requestUrl, clientIp,
                    memberIdString, params);
        }
    }

    /**
     * api response 로그 출력
     *
     * @param request       request 객체
     * @param response      response 객체
     * @param executionTime 실행 시간
     */
    public void apiResponse(HttpServletRequest request, HttpServletResponse response, long executionTime) {
        String requestId = MDC.get("requestId");
        String requestUrl = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = getClientIp(request);
        int statusCode = response.getStatus();
        
        log.info("[{}] [{}] {} | IP: {} | Status: {} | Execution Time: {}ms", requestId, httpMethod, requestUrl,
                clientIp, statusCode, executionTime);
    }

    /**
     * 클라이언트 ip 추출 메소드
     *
     * @param request request 객체
     * @return ip
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * request param을 JSON 형태 String으로 변환
     *
     * @param request request 객체
     * @return string
     */
    private String getParamsAsString(HttpServletRequest request) {
        try {
            return objectMapper.writeValueAsString(request.getParameterMap());
        } catch (JsonProcessingException e) {
            return "Unable to parse parameters";
        }
    }

}
