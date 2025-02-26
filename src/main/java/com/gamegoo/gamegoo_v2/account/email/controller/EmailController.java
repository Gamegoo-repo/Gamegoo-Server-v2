package com.gamegoo.gamegoo_v2.account.email.controller;

import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.account.email.dto.EmailCodeRequest;
import com.gamegoo.gamegoo_v2.account.email.dto.EmailRequest;
import com.gamegoo.gamegoo_v2.account.email.service.EmailFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Email", description = "Email 전송 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/email")
@Validated
public class EmailController {

    private final EmailFacadeService emailFacadeService;

    @PostMapping("/send/join")
    @Operation(summary = "회원가입용 이메일 인증코드 전송 API 입니다. 중복확인 포함", description = "API for sending email for join")
    public ApiResponse<String> sendEmailWithCheckDuplication(
            @Valid @RequestBody EmailRequest request) {
        return ApiResponse.ok(emailFacadeService.sendEmailVerificationCodeCheckDuplication(request));
    }

    @PostMapping("/send/pwd")
    @Operation(summary = "비밀번호 찾기용 이메일 인증코드 전송 API 입니다.", description = "API for sending email for finding password")
    public ApiResponse<String> sendEmail(@Valid @RequestBody EmailRequest request) {
        return ApiResponse.ok(emailFacadeService.sendEmailVerificationCodeCheckExistence(request));
    }

    @PostMapping("/verify")
    @Operation(summary = "이메일 인증코드 검증 API 입니다.", description = "API for verifying email")
    public ApiResponse<String> verifyEmail(@Valid @RequestBody EmailCodeRequest request) {
        return ApiResponse.ok(emailFacadeService.verifyEmailCode(request));
    }

}
