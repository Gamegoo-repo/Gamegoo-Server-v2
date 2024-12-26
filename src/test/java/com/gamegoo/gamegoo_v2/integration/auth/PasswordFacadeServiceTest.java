package com.gamegoo.gamegoo_v2.integration.auth;

import com.gamegoo.gamegoo_v2.account.auth.dto.PasswordRequest;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class PasswordFacadeServiceTest {

    @Autowired
    PasswordFacadeService passwordFacadeService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EmailVerifyRecordRepository emailVerifyRecordRepository;

    private Member member;
    private EmailVerifyRecord emailVerifyRecord;

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
        emailVerifyRecord = createEmailVerifyRecord(EMAIL, VERIFY_CODE);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("jwt 토큰 없이 비밀번호 변경 성공")
    @Test
    void resetPassword() {
        // given
        PasswordRequest request = PasswordRequest.builder()
                .email(EMAIL)
                .verifyCode(VERIFY_CODE)
                .newPassword(NEW_PASSWORD)
                .build();

        // when
        passwordFacadeService.changePassword(request);

        // then
        Member updatedMember = memberRepository.findByEmail(EMAIL)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        assertTrue(PasswordUtil.matchesPassword(request.getNewPassword(), updatedMember.getPassword()),
                "비밀번호가 올바르게 변경되어야 합니다.");
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

    private EmailVerifyRecord createEmailVerifyRecord(String email, String verifyCode) {
        return emailVerifyRecordRepository.save(EmailVerifyRecord.builder()
                .email(email)
                .code(verifyCode)
                .build());
    }

}
