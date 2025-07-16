package com.gamegoo.gamegoo_v2.feature;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.account.member.service.BanService;
import com.gamegoo.gamegoo_v2.core.common.validator.BanValidator;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("제재 기능 통합 테스트")
class BanFeatureTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberRecentStatsRepository memberRecentStatsRepository;

    @Autowired
    private BanService banService;

    @Autowired
    private BanValidator banValidator;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = createTestMember();
    }

    @AfterEach
    void tearDown() {
        memberRecentStatsRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("제재를 적용하면 사용자가 제재 상태가 된다")
    void when_ban_applied_user_becomes_banned() {
        // when
        banService.applyBan(testMember, BanType.BAN_1D);

        // then
        assertThat(testMember.getBanType()).isEqualTo(BanType.BAN_1D);
        assertThat(testMember.isBanned()).isTrue();
        assertThat(testMember.getBanExpireAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("제재를 해제하면 사용자가 정상 상태가 된다")
    void when_ban_released_user_becomes_normal() {
        // given
        banService.applyBan(testMember, BanType.BAN_1D);
        assertThat(testMember.isBanned()).isTrue();

        // when
        banService.releaseBan(testMember);

        // then
        assertThat(testMember.getBanType()).isEqualTo(BanType.NONE);
        assertThat(testMember.isBanned()).isFalse();
        assertThat(testMember.getBanExpireAt()).isNull();
    }

    @Test
    @DisplayName("제재된 사용자는 매칭 검증에서 예외가 발생한다")
    void banned_user_fails_matching_validation() {
        // given
        banService.applyBan(testMember, BanType.BAN_1D);

        // when & then
        assertThatThrownBy(() -> banValidator.throwIfBannedFromMatching(testMember))
                .isInstanceOf(MemberException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_MATCHING.getCode());
    }

    @Test
    @DisplayName("제재된 사용자는 채팅 검증에서 예외가 발생한다")
    void banned_user_fails_chatting_validation() {
        // given
        banService.applyBan(testMember, BanType.BAN_1D);

        // when & then
        assertThatThrownBy(() -> banValidator.throwIfBannedFromChatting(testMember))
                .isInstanceOf(MemberException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_CHATTING.getCode());
    }

    @Test
    @DisplayName("제재된 사용자는 게시글 작성 검증에서 예외가 발생한다")
    void banned_user_fails_posting_validation() {
        // given
        banService.applyBan(testMember, BanType.BAN_1D);

        // when & then
        assertThatThrownBy(() -> banValidator.throwIfBannedFromPosting(testMember))
                .isInstanceOf(MemberException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_POSTING.getCode());
    }

    @Test
    @DisplayName("경고는 실제 제재가 아니므로 모든 검증을 통과한다")
    void warning_passes_all_validations() {
        // given
        banService.applyBan(testMember, BanType.WARNING);

        // when & then - 예외가 발생하지 않아야 함
        banValidator.throwIfBannedFromMatching(testMember);
        banValidator.throwIfBannedFromChatting(testMember);
        banValidator.throwIfBannedFromPosting(testMember);

        assertThat(testMember.isBanned()).isFalse();
    }

    @Test
    @DisplayName("영구 제재 사용자는 모든 검증에서 실패한다")
    void permanent_ban_fails_all_validations() {
        // given
        banService.applyBan(testMember, BanType.PERMANENT);

        // when & then
        assertThatThrownBy(() -> banValidator.throwIfBannedFromMatching(testMember))
                .isInstanceOf(MemberException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_MATCHING.getCode());

        assertThatThrownBy(() -> banValidator.throwIfBannedFromChatting(testMember))
                .isInstanceOf(MemberException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_CHATTING.getCode());

        assertThatThrownBy(() -> banValidator.throwIfBannedFromPosting(testMember))
                .isInstanceOf(MemberException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_POSTING.getCode());

        assertThat(testMember.isBanned()).isTrue();
        assertThat(testMember.getBanExpireAt()).isNull(); // 영구 제재는 만료시간이 없음
    }

    @Test
    @DisplayName("제재 만료 확인이 정상적으로 동작한다")
    void ban_expiry_check_works_correctly() {
        // given - 과거 시간으로 제재 설정
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        testMember.applyBan(BanType.BAN_1D, pastTime);
        memberRepository.save(testMember);

        // when
        banService.checkBanExpiry(testMember);

        // then - 만료된 제재는 자동 해제되어야 함
        assertThat(testMember.getBanType()).isEqualTo(BanType.NONE);
        assertThat(testMember.isBanned()).isFalse();
        assertThat(testMember.getBanExpireAt()).isNull();
    }

    @Test
    @DisplayName("제재 해제 후 모든 검증을 통과한다")
    void after_ban_release_passes_all_validations() {
        // given
        banService.applyBan(testMember, BanType.BAN_1D);
        assertThat(testMember.isBanned()).isTrue();

        // when
        banService.releaseBan(testMember);

        // then - 모든 검증이 통과해야 함
        banValidator.throwIfBannedFromMatching(testMember);
        banValidator.throwIfBannedFromChatting(testMember);
        banValidator.throwIfBannedFromPosting(testMember);

        assertThat(testMember.isBanned()).isFalse();
    }

    @Test
    @DisplayName("제재 상태 확인이 정확히 동작한다")
    void ban_status_check_works_correctly() {
        // given - 제재되지 않은 상태
        assertThat(banValidator.isBanned(testMember)).isFalse();

        // when - 제재 적용
        banService.applyBan(testMember, BanType.BAN_1D);

        // then - 제재 상태로 변경
        assertThat(banValidator.isBanned(testMember)).isTrue();

        // when - 제재 해제
        banService.releaseBan(testMember);

        // then - 다시 정상 상태로 변경
        assertThat(banValidator.isBanned(testMember)).isFalse();
    }

    private Member createTestMember() {
        Member member = Member.createForGeneral(
                "test@example.com",
                "password",
                LoginType.GENERAL,
                "testUser",
                "KR1",
                Tier.BRONZE,
                1,
                50.0,
                Tier.BRONZE,
                1,
                50.0,
                10,
                5,
                true
        );

        memberRecentStatsRepository.save(MemberRecentStats.builder()
                .member(member)
                .build());

        return memberRepository.save(member);
    }

}
