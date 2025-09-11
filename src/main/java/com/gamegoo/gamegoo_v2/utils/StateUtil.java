package com.gamegoo.gamegoo_v2.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.gamegoo_v2.core.exception.RiotException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.external.riot.domain.RSOState;

import java.net.URLDecoder;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StateUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static RSOState decodeRSOState(String rawState) {
        // state가 없는 경우
        if (rawState == null || rawState.isBlank()) {
            throw new RiotException(ErrorCode.RSO_NO_STATE);
        }

        try {
            String urlDecoded = URLDecoder.decode(rawState, UTF_8);
            byte[] b64 = Base64.getUrlDecoder().decode(urlDecoded);
            String json = new String(b64, UTF_8);
            return mapper.readValue(json, RSOState.class);
        } catch (Exception e) {
            throw new RiotException(ErrorCode.STATE_WRONG_DECODE);
        }
    }

}
