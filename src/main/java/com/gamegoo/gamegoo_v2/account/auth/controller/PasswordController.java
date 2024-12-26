package com.gamegoo.gamegoo_v2.account.auth.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.auth.dto.PasswordResetRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.PasswordResetWithVerifyRequest;
import com.gamegoo.gamegoo_v2.account.auth.service.PasswordFacadeService;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/password")
public class PasswordController {

    private final PasswordFacadeService passwordFacadeService;

    @PostMapping("/reset")
    @Operation(summary = "비밀번호 재설정 API 입니다. JWT X", description = "API for reseting password JWT X")
    public ApiResponse<String> resetPassword(@Valid @RequestBody PasswordResetWithVerifyRequest request) {
        return ApiResponse.ok(passwordFacadeService.changePasswordWithVerify(request));
    }

    @PutMapping("/change")
    @Operation(summary = "비밀번호 재설정 API 입니다. JWT O", description = "API for reseting password JWT O")
    public ApiResponse<String> resetPasswordWithJWT(@AuthMember Member member, @Valid @RequestBody PasswordResetRequest request) {
        return ApiResponse.ok(passwordFacadeService.changePassword(member, request));
    }

}
