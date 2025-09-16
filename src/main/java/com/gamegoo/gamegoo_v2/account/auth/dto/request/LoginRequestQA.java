package com.gamegoo.gamegoo_v2.account.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequestQA {

    @NotBlank(message = "Email은 비워둘 수 없습니다.")
    private final String email;

    @NotBlank(message = "password는 비워둘 수 없습니다.")
    private final String password;

}
