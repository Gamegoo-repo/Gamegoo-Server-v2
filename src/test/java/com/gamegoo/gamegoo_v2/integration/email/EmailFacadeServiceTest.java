package com.gamegoo.gamegoo_v2.integration.email;

import com.gamegoo.gamegoo_v2.account.email.domain.EmailVerifyRecord;
import com.gamegoo.gamegoo_v2.account.email.dto.EmailRequest;
import com.gamegoo.gamegoo_v2.account.email.repository.EmailVerifyRecordRepository;
import com.gamegoo.gamegoo_v2.account.email.service.EmailFacadeService;
import com.gamegoo.gamegoo_v2.account.email.service.EmailService;
import com.gamegoo.gamegoo_v2.core.exception.EmailException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;

@ActiveProfiles("test")
@SpringBootTest
class EmailFacadeServiceTest {

    @Autowired
    private EmailVerifyRecordRepository emailVerifyRecordRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailFacadeService emailFacadeService;

    @MockitoSpyBean
    private EmailService emailService;

    private static final String EMAIL = "test@example.com";
    private static final String GAMENAME = "test";

    @AfterEach
    void tearDown() {
        emailVerifyRecordRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("중복확인 포함한 이메일 인증 코드 전송")
    class sendEmailVerificationCode {

        @DisplayName("중복확인 포함한 이메일 인증 코드 전송 성공")
        @Test
        void sendEmailVerificationCode_ShouldSendSuccessfully() {
            // Given
            EmailRequest request = EmailRequest.builder()
                    .email(EMAIL)
                    .build();

            // 실제로 이메일이 전송되지 않도록 Mock
            doNothing().when(emailService).sendEmail(any(), any(), any(), any());

            // When // Then
            assertDoesNotThrow(() -> emailFacadeService.sendEmailVerificationCodeCheckDuplication(request));

            // 이메일 인증 레코드 저장 확인
            List<EmailVerifyRecord> recordList = emailVerifyRecordRepository.findAll();
            assertThat(recordList).hasSize(1);
        }

        @DisplayName("중복확인 포함한 이메일 인증 코드 전송 실패: 3분 이내에 3번 이상 요청을 보낸 경우 예외가 발생한다.")
        @Test
        void sendEmailVerificationCode_ShouldThrowWhenTooManyRequests() {
            // Given
            EmailRequest request = EmailRequest.builder()
                    .email(EMAIL)
                    .build();

            // 실제로 이메일이 전송되지 않도록 Mock
            doNothing().when(emailService).sendEmail(any(), any(), any(), any());

            // 인증코드 요청 3개 생성
            emailVerifyRecordRepository.save(EmailVerifyRecord.create(EMAIL, "code1"));
            emailVerifyRecordRepository.save(EmailVerifyRecord.create(EMAIL, "code2"));
            emailVerifyRecordRepository.save(EmailVerifyRecord.create(EMAIL, "code3"));

            // When // Then
            assertThatThrownBy(() -> emailFacadeService.sendEmailVerificationCodeCheckDuplication(request))
                    .isInstanceOf(EmailException.class)
                    .hasMessage(ErrorCode.EMAIL_LIMIT_EXCEEDED.getMessage());
        }

        @DisplayName("중복확인 포함한 이메일 인증 코드 전송 실패: 메일 서버 장애로 인해 이메일 전송에 실패한 경우 예외가 발생한다.")
        @Test
        void sendEmail_ShouldThrowWhenMailServerDown() {
            // Given
            EmailRequest request = EmailRequest.builder()
                    .email(EMAIL)
                    .build();

            // 이메일 전송 실패 mock 처리
            willThrow(new EmailException(ErrorCode.EMAIL_SEND_FAIL))
                    .given(emailService).sendEmail(any(), any(), any(), any());

            // When // Then
            assertThatThrownBy(() -> emailFacadeService.sendEmailVerificationCodeCheckDuplication(request))
                    .isInstanceOf(EmailException.class)
                    .hasMessage(ErrorCode.EMAIL_SEND_FAIL.getMessage());
        }

        @DisplayName("중복확인 포함한 이메일 인증 코드 전송 실패: 이미 등록된 메일인 경우 예외가 발생한다.")
        @Test
        void sendEmail_ShouldThrownWhenDuplicatedEmail() {
            // given
            EmailRequest request = EmailRequest.builder()
                    .email(EMAIL)
                    .build();

            createMember(EMAIL, GAMENAME);

            // when // then
            assertThatThrownBy(() -> emailFacadeService.sendEmailVerificationCodeCheckDuplication(request))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_ALREADY_EXISTS.getMessage());
        }

    }

    private Member createMember(String email, String gameName) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("testPassword")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag("TAG")
                .soloTier(Tier.IRON)
                .soloRank(0)
                .soloWinRate(0.0)
                .soloGameCount(0)
                .freeTier(Tier.IRON)
                .freeRank(0)
                .freeWinRate(0.0)
                .freeGameCount(0)
                .isAgree(true)
                .build());
    }

}
