package com.gamegoo.gamegoo_v2.account.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminLoginRequest {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "관리자 계정 (gameName#tag 형식)", example = "admin#ADM")
    @NotBlank(message = "account는 비워둘 수 없습니다")
    private String account;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "공통 비밀번호")
    @NotBlank(message = "password는 비워둘 수 없습니다")
    private String password;

}