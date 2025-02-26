package com.gamegoo.gamegoo_v2.account.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RefreshTokenResponse {
    Long id;
    String accessToken;
    String refreshToken;

    public static RefreshTokenResponse of(Long id, String accessToken, String refreshToken) {
        return RefreshTokenResponse.builder()
                .id(id)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
