package com.gamegoo.gamegoo_v2.integration.auth;

import com.gamegoo.gamegoo_v2.account.auth.dto.request.PasswordCheckRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.PasswordResetRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.PasswordResetWithVerifyRequest;
import com.gamegoo.gamegoo_v2.account.auth.service.PasswordFacadeService;
import com.gamegoo.gamegoo_v2.account.email.domain.EmailVerifyRecord;
import com.gamegoo.gamegoo_v2.account.email.repository.EmailVerifyRecordRepository;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.utils.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PasswordFacadeServiceTest {

    @Autowired
    private PasswordFacadeService passwordFacadeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailVerifyRecordRepository emailVerifyRecordRepository;

    private Member member;

    private static final String EMAIL = "test@gmail.com";
    private static final String PASSWORD = "testpassword";
    private static final String NEW_PASSWORD = "newpassword";
    private static final String VERIFY_CODE = "123456";
    private static final String GAMENAME = "test1";

    @BeforeEach
    void setUp() {
        // Member 테스트용 객체 생성
        member = createMember(EMAIL, GAMENAME, PasswordUtil.encodePassword(PASSWORD));

        // EmailVerifyRecord 생성
        createEmailVerifyRecord(EMAIL, VERIFY_CODE);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        emailVerifyRecordRepository.deleteAllInBatch();
    }

    @DisplayName("jwt 토큰 없을때 비밀번호 변경 성공")
    @Test
    void resetPassword() {
        // given
        PasswordResetWithVerifyRequest request = PasswordResetWithVerifyRequest.builder()
                .email(EMAIL)
                .verifyCode(VERIFY_CODE)
                .newPassword(NEW_PASSWORD)
                .build();

        // when
        passwordFacadeService.changePasswordWithVerify(request);

        // then
        Member updatedMember = memberRepository.findByEmail(EMAIL)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        assertThat(PasswordUtil.matchesPassword(request.getNewPassword(), updatedMember.getPassword()))
                .as("비밀번호가 올바르게 변경되어야 합니다.")
                .isTrue();
    }

    @DisplayName("jwt 토큰 있을 때 비밀번호 변경 성공")
    @Test
    void resetPasswordJWT() {
        // given
        PasswordResetRequest request = PasswordResetRequest.builder()
                .newPassword(NEW_PASSWORD)
                .build();

        // when
        passwordFacadeService.changePassword(member, request);

        // then
        assertThat(PasswordUtil.matchesPassword(request.getNewPassword(), member.getPassword()))
                .as("비밀번호가 올바르게 변경되어야 합니다.")
                .isTrue();
    }

    @DisplayName("비밀번호 확인 테스트 : 일치할 경우")
    @Test
    void checkPasswordMatch() {
        // given
        PasswordCheckRequest request = PasswordCheckRequest.builder()
                .password(PASSWORD)
                .build();

        // when
        passwordFacadeService.checkPassword(member, request);

        // then
        assertThat(PasswordUtil.matchesPassword(request.getPassword(), member.getPassword()))
                .as("비밀번호가 일치해야합니다.")
                .isTrue();
    }

    @DisplayName("비밀번호 확인 테스트 : 불일치할 경우")
    @Test
    void checkPasswordUnMatch() {
        // given
        PasswordCheckRequest request = PasswordCheckRequest.builder()
                .password(NEW_PASSWORD)
                .build();

        // when
        passwordFacadeService.checkPassword(member, request);

        // then
        assertThat(PasswordUtil.matchesPassword(request.getPassword(), member.getPassword()))
                .as("비밀번호가 불일치해야합니다.")
                .isFalse();
    }

    private Member createMember(String email, String gameName, String password) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password(password)
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag("TAG")
                .tier(Tier.IRON)
                .gameRank(0)
                .winRate(0.0)
                .gameCount(0)
                .isAgree(true)
                .build());
    }

    private void createEmailVerifyRecord(String email, String verifyCode) {
        emailVerifyRecordRepository.save(EmailVerifyRecord.builder()
                .email(email)
                .code(verifyCode)
                .build());
    }

}
