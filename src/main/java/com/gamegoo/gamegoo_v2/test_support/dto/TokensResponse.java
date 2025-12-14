package com.gamegoo.gamegoo_v2.test_support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokensResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String accessToken;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String refreshToken;

    public static TokensResponse of(String accessToken, String refreshToken) {
        return TokensResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
