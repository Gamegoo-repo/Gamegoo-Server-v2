package com.gamegoo.gamegoo_v2.account.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class JoinRequest {

    @Email(message = "Email 형식이 올바르지 않습니다.")
    @NotBlank(message = "Email은 비워둘 수 없습니다.")
    String email;

    @NotBlank(message = "password는 비워둘 수 없습니다.")
    String password;

    @NotBlank(message = "gameName 값은 비워둘 수 없습니다.")
    String gameName;

    @NotBlank(message = "tag 값은 비워둘 수 없습니다.")
    String tag;

    @NotNull(message = "isAgree 값은 비워둘 수 없습니다. true/false 둘 중 하나를 반드시 포함해야합니다.")
    Boolean isAgree;

}
