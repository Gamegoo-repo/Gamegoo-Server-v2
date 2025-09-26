package com.gamegoo.gamegoo_v2.account.auth.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.RefreshTokenRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.response.RefreshTokenResponse;
import com.gamegoo.gamegoo_v2.account.auth.service.AuthFacadeService;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/auth")
public class AuthController {

    private final AuthFacadeService authFacadeService;

    @GetMapping("/token/{memberId}")
    @Operation(summary = "임시 access token 발급 API", description = "테스트용으로 access token을 발급받을 수 있는 API 입니다.")
    @Parameter(name = "memberId", description = "대상 회원의 id 입니다.")
    public ApiResponse<String> getTestAccessToken(@PathVariable(name = "memberId") Long memberId) {
        return ApiResponse.ok(authFacadeService.createTestAccessToken(memberId));
    }

    @PostMapping("/logout")
    @Operation(summary = "logout API 입니다.", description = "API for logout")
    public ApiResponse<String> logout(@AuthMember Member member) {
        return ApiResponse.ok(authFacadeService.logout(member));
    }

    @PostMapping("/refresh")
    @Operation(summary = "refresh   토큰을 통한 access, refresh 토큰 재발급 API 입니다.", description = "API for Refresh Token")
    public ApiResponse<RefreshTokenResponse> updateToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authFacadeService.updateToken(request));
    }

    @DeleteMapping
    @Operation(summary = "탈퇴 API입니다.", description = "API for Blinding Member")
    public ApiResponse<String> blindMember(@AuthMember Member member) {
        return ApiResponse.ok(authFacadeService.blindMember(member));
    }

}
