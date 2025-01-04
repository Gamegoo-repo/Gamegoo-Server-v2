package com.gamegoo.gamegoo_v2.account.auth.service;

import com.gamegoo.gamegoo_v2.account.auth.dto.request.PasswordCheckRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.PasswordResetRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.PasswordResetWithVerifyRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.response.PasswordCheckResponse;
import com.gamegoo.gamegoo_v2.account.email.service.EmailService;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordFacadeService {

    private final PasswordService passwordService;
    private final EmailService emailService;
    private final MemberService memberService;

    /**
     * 비밀번호 변경 & 이메일 인증
     * @param request   이메일,인증코드,새로운 비밀번호
     * @return          성공 메세지
     */
    @Transactional
    public String changePasswordWithVerify(PasswordResetWithVerifyRequest request) {
        // 이메일, 코드 검증
        emailService.verifyEmailCode(request.getEmail(), request.getVerifyCode());
        Member member = memberService.findMemberByEmail(request.getEmail());

        // 새로운 비밀번호 설정
        passwordService.changePassword(member, request.getNewPassword());
        return "비밀번호 변경이 완료되었습니다.";
    }

    /**
     * 비밀번호 변경
     * @param member    사용자
     * @param request   새로운 비밀번호
     * @return          성공 메세지
     */
    @Transactional
    public String changePassword(Member member, PasswordResetRequest request) {
        // 새로운 비밀번호 설정
        passwordService.changePassword(member, request.getNewPassword());
        return "비밀번호 변경이 완료되었습니다.";
    }

    /**
     * 비밀번호 확인
     * @param member    사용자
     * @param request   비밀번호
     * @return          일치 여부
     */
    public PasswordCheckResponse checkPassword(Member member, PasswordCheckRequest request) {
        return PasswordCheckResponse.of(passwordService.checkPassword(member, request.getPassword()));
    }

}
