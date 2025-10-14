package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotAuthTokenResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessToken;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String idToken;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String redirectUri;
}
