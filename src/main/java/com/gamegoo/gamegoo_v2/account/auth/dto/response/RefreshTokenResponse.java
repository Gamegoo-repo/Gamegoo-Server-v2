package com.gamegoo.gamegoo_v2.account.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RefreshTokenResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String accessToken;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String refreshToken;

    public static RefreshTokenResponse of(Long id, String accessToken, String refreshToken) {
        return RefreshTokenResponse.builder()
                .id(id)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
