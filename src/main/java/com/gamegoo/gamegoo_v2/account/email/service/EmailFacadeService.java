package com.gamegoo.gamegoo_v2.account.email.service;

import com.gamegoo.gamegoo_v2.account.email.dto.EmailCodeRequest;
import com.gamegoo.gamegoo_v2.account.email.dto.EmailRequest;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailFacadeService {

    private final EmailService emailService;
    private final MemberService memberService;

    /**
     * 이메일 인증코드 검증
     * @param request   이메일, 인증코드
     * @return          성공 메세지
     */
    public String verifyEmailCode(EmailCodeRequest request) {
        emailService.verifyEmailCode(request.getEmail(), request.getCode());
        return "인증이 완료되었습니다.";
    }

    /**
     * 중복확인 후 이메일 인증코드 전송
     * @param request   이메일
     * @return          성공 메세지
     */
    @Transactional
    public String sendEmailVerificationCodeCheckDuplication(EmailRequest request) {
        memberService.checkDuplicateMemberByEmail(request.getEmail());
        emailService.sendEmailVerificationCode(request.getEmail());
        return "인증 이메일을 발송했습니다.";
    }

    /**
     * DB에 있는 사용자인지 확인 후 인증코드 전송
     * @param request   이메일
     * @return          성공 메세지
     */
    @Transactional
    public String sendEmailVerificationCodeCheckExistence(EmailRequest request) {
        memberService.checkExistMemberByEmail(request.getEmail());
        emailService.sendEmailVerificationCode(request.getEmail());
        return "인증 이메일을 발송했습니다.";
    }

}
