package com.gamegoo.gamegoo_v2.account.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PasswordRequest {

    @Email(message = "Email 형식이 올바르지 않습니다.")
    @NotBlank(message = "Email은 비워둘 수 없습니다.")
    String email;

    @NotBlank(message = "verifyCode는 비워둘 수 없습니다.")
    String verifyCode;

    @NotBlank(message = "newPassword는 비워둘 수 없습니다.")
    String newPassword;

}
