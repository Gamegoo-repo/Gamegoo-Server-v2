package com.gamegoo.gamegoo_v2.account.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordCheckResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean isTrue;

    public static PasswordCheckResponse of(boolean isTrue) {
        return PasswordCheckResponse.builder().isTrue(isTrue).build();
    }

}
