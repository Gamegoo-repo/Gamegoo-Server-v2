package com.gamegoo.gamegoo_v2.account.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PasswordResetRequest {

    @NotBlank(message = "newPassword는 비워둘 수 없습니다.")
    String newPassword;

}
