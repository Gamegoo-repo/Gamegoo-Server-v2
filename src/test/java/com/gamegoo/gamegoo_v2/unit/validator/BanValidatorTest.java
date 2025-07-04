package com.gamegoo.gamegoo_v2.unit.validator;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.service.BanService;
import com.gamegoo.gamegoo_v2.core.common.validator.BanValidator;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BanValidator 단위 테스트")
class BanValidatorTest {

    @Mock
    private BanService banService;

    @InjectMocks
    private BanValidator banValidator;

    private Member normalMember;
    private Member bannedMember;

    @BeforeEach
    void setUp() {
        normalMember = createMember(BanType.NONE, null);
        bannedMember = createMember(BanType.BAN_1D, LocalDateTime.now().plusDays(1));
    }

    @Nested
    @DisplayName("게시글 작성 제재 검증")
    class PostingBanValidationTest {

        @Test
        @DisplayName("제재되지 않은 사용자는 게시글 작성 검증을 통과한다")
        void non_banned_user_passes_posting_validation() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then - 예외가 발생하지 않아야 함
            banValidator.throwIfBannedFromPosting(normalMember);

            verify(banService).checkBanExpiry(normalMember);
        }

        @Test
        @DisplayName("게시글 작성이 제재된 사용자는 MemberException이 발생한다")
        void banned_user_fails_posting_validation() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then
            assertThatThrownBy(() -> banValidator.throwIfBannedFromPosting(bannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_POSTING.getCode())
                    .hasFieldOrPropertyWithValue("message", ErrorCode.MEMBER_BANNED_FROM_POSTING.getMessage());

            verify(banService).checkBanExpiry(bannedMember);
        }
    }

    @Nested
    @DisplayName("채팅 제재 검증")
    class ChattingBanValidationTest {

        @Test
        @DisplayName("제재되지 않은 사용자는 채팅 검증을 통과한다")
        void non_banned_user_passes_chatting_validation() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then - 예외가 발생하지 않아야 함
            banValidator.throwIfBannedFromChatting(normalMember);

            verify(banService).checkBanExpiry(normalMember);
        }

        @Test
        @DisplayName("채팅이 제재된 사용자는 MemberException이 발생한다")
        void banned_user_fails_chatting_validation() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then
            assertThatThrownBy(() -> banValidator.throwIfBannedFromChatting(bannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_CHATTING.getCode())
                    .hasFieldOrPropertyWithValue("message", ErrorCode.MEMBER_BANNED_FROM_CHATTING.getMessage());

            verify(banService).checkBanExpiry(bannedMember);
        }
    }

    @Nested
    @DisplayName("매칭 제재 검증")
    class MatchingBanValidationTest {

        @Test
        @DisplayName("제재되지 않은 사용자는 매칭 검증을 통과한다")
        void non_banned_user_passes_matching_validation() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then - 예외가 발생하지 않아야 함
            banValidator.throwIfBannedFromMatching(normalMember);

            verify(banService).checkBanExpiry(normalMember);
        }

        @Test
        @DisplayName("매칭이 제재된 사용자는 MemberException이 발생한다")
        void banned_user_fails_matching_validation() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then
            assertThatThrownBy(() -> banValidator.throwIfBannedFromMatching(bannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_MATCHING.getCode())
                    .hasFieldOrPropertyWithValue("message", ErrorCode.MEMBER_BANNED_FROM_MATCHING.getMessage());

            verify(banService).checkBanExpiry(bannedMember);
        }
    }

    @Nested
    @DisplayName("전체 제재 검증")
    class GeneralBanValidationTest {

        @Test
        @DisplayName("제재되지 않은 사용자는 전체 제재 검증을 통과한다")
        void non_banned_user_passes_general_ban_validation() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then - 예외가 발생하지 않아야 함
            banValidator.throwIfBanned(normalMember);

            verify(banService).checkBanExpiry(normalMember);
        }

        @Test
        @DisplayName("제재된 사용자는 전체 제재 검증에서 MemberException이 발생한다")
        void banned_user_fails_general_ban_validation() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then
            assertThatThrownBy(() -> banValidator.throwIfBanned(bannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED.getCode())
                    .hasFieldOrPropertyWithValue("message", ErrorCode.MEMBER_BANNED.getMessage());

            verify(banService).checkBanExpiry(bannedMember);
        }
    }

    @Nested
    @DisplayName("제재 상태 확인")
    class BanStatusCheckTest {

        @Test
        @DisplayName("제재되지 않은 사용자의 제재 상태는 false를 반환한다")
        void non_banned_user_status_check_returns_false() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when
            boolean result = banValidator.isBanned(normalMember);

            // then
            assertThat(result).isFalse();
            verify(banService).checkBanExpiry(normalMember);
        }

        @Test
        @DisplayName("제재된 사용자의 제재 상태는 true를 반환한다")
        void banned_user_status_check_returns_true() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when
            boolean result = banValidator.isBanned(bannedMember);

            // then
            assertThat(result).isTrue();
            verify(banService).checkBanExpiry(bannedMember);
        }
    }

    @Nested
    @DisplayName("제재 만료 확인")
    class BanExpiryCheckTest {

        @Test
        @DisplayName("모든 검증 메서드는 제재 만료를 확인한다")
        void all_validation_methods_check_ban_expiry() {
            // given
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when
            banValidator.throwIfBannedFromPosting(normalMember);
            banValidator.throwIfBannedFromChatting(normalMember);
            banValidator.throwIfBannedFromMatching(normalMember);
            banValidator.throwIfBanned(normalMember);
            banValidator.isBanned(normalMember);

            // then
            verify(banService, org.mockito.Mockito.times(5)).checkBanExpiry(normalMember);
        }
    }

    @Nested
    @DisplayName("경고 타입 제재")
    class WarningBanTypeTest {

        @Test
        @DisplayName("경고 타입 제재는 실제 제재가 아니므로 모든 검증을 통과한다")
        void warning_ban_type_allows_all_functions() {
            // given
            Member warningMember = createMember(BanType.WARNING, null);
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then - 모든 검증이 통과해야 함
            banValidator.throwIfBannedFromPosting(warningMember);
            banValidator.throwIfBannedFromChatting(warningMember);
            banValidator.throwIfBannedFromMatching(warningMember);
            banValidator.throwIfBanned(warningMember);

            boolean isBanned = banValidator.isBanned(warningMember);
            assertThat(isBanned).isFalse();

            verify(banService, org.mockito.Mockito.times(5)).checkBanExpiry(warningMember);
        }
    }

    @Nested
    @DisplayName("영구 제재")
    class PermanentBanTest {

        @Test
        @DisplayName("영구 제재된 사용자는 모든 검증에서 실패한다")
        void permanently_banned_user_fails_all_validations() {
            // given
            Member permanentBannedMember = createMember(BanType.PERMANENT, null);
            doNothing().when(banService).checkBanExpiry(any(Member.class));

            // when & then
            assertThatThrownBy(() -> banValidator.throwIfBannedFromPosting(permanentBannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_POSTING.getCode());

            assertThatThrownBy(() -> banValidator.throwIfBannedFromChatting(permanentBannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_CHATTING.getCode());

            assertThatThrownBy(() -> banValidator.throwIfBannedFromMatching(permanentBannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_MATCHING.getCode());

            assertThatThrownBy(() -> banValidator.throwIfBanned(permanentBannedMember))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED.getCode());

            boolean isBanned = banValidator.isBanned(permanentBannedMember);
            assertThat(isBanned).isTrue();
        }
    }

    // 헬퍼 메서드
    private Member createMember(BanType banType, LocalDateTime banExpireAt) {
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

        // 제재 정보 설정
        if (banType != BanType.NONE) {
            member.applyBan(banType, banExpireAt);
        }

        return member;
    }
}
