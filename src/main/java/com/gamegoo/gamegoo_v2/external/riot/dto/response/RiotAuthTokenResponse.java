package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotAuthTokenResponse {
    private String accessToken;
    private String idToken;
    private String refreshToken;
    private String redirectUri;
}
