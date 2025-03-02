package com.gamegoo.gamegoo_v2.utils;

import com.gamegoo.gamegoo_v2.core.exception.RiotException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class RiotApiHelper {
    /**
     * Riot API 호출 실패 시 예외 처리
     */
    public void handleApiError(Exception e) {
        if (e instanceof HttpClientErrorException httpEx) {
            if (httpEx.getStatusCode() == HttpStatus.BAD_REQUEST||httpEx.getStatusCode() == HttpStatus.UNAUTHORIZED||httpEx.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new RiotException(ErrorCode.RIOT_INVALID_API_KEY);
            }

            if (httpEx.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
            }

            throw new RiotException(ErrorCode.RIOT_API_ERROR,httpEx.getMessage());
        }

        if (e instanceof HttpServerErrorException) {
            throw new RiotException(ErrorCode.RIOT_SERVER_ERROR);
        }

        if (e instanceof ResourceAccessException) {
            throw new RiotException(ErrorCode.RIOT_NETWORK_ERROR);
        }

        throw new RiotException(ErrorCode.RIOT_UNKNOWN_ERROR, e.getMessage());
    }
}
