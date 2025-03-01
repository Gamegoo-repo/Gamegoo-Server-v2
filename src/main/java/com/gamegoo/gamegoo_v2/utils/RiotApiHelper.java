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
            if (httpEx.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = httpEx.getResponseBodyAsString();
                if (responseBody.contains("Unknown apikey")) {
                    throw new RiotException(ErrorCode.RIOT_INVALID_API_KEY);
                }
                throw new RiotException(ErrorCode.RIOT_API_BAD_REQUEST);
            }

            if (httpEx.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
            }

            throw new RiotException(ErrorCode.RIOT_API_ERROR, httpEx.getMessage());
        }

        throw new RiotException(ErrorCode.RIOT_UNKNOWN_ERROR, e.getMessage());
    }
}
