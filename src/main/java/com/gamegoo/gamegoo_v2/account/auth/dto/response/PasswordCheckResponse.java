package com.gamegoo.gamegoo_v2.account.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordCheckResponse {

    boolean isTrue;

    public static PasswordCheckResponse of(boolean isTrue) {
        return PasswordCheckResponse.builder().isTrue(isTrue).build();
    }

}
