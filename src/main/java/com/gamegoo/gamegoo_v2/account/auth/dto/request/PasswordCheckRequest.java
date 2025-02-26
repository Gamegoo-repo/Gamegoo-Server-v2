package com.gamegoo.gamegoo_v2.account.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordCheckRequest {

    @NotBlank(message = "password는 비워둘 수 없습니다")
    String password;

}
