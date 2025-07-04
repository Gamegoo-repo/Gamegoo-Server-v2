package com.gamegoo.gamegoo_v2.domain.member;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Member 도메인 테스트")
class MemberTest {

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.createForGeneral(
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
    }

    @Test
    @DisplayName("제재 적용 테스트")
    void applyBan_Success() {
        // given
        LocalDateTime expireAt = LocalDateTime.now().plusDays(1);

        // when
        member.applyBan(BanType.BAN_1D, expireAt);

        // then
        assertThat(member.getBanType()).isEqualTo(BanType.BAN_1D);
        assertThat(member.getBanExpireAt()).isEqualTo(expireAt);
    }

    @Test
    @DisplayName("제재 해제 테스트")
    void releaseBan_Success() {
        // given
        member.applyBan(BanType.BAN_1D, LocalDateTime.now().plusDays(1));

        // when
        member.releaseBan();

        // then
        assertThat(member.getBanType()).isEqualTo(BanType.NONE);
        assertThat(member.getBanExpireAt()).isNull();
    }

    @Test
    @DisplayName("제재 상태 확인 - 제재 없음")
    void isBanned_NoBan_ReturnsFalse() {
        // when
        boolean result = member.isBanned();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("제재 상태 확인 - 영구 제재")
    void isBanned_PermanentBan_ReturnsTrue() {
        // given
        member.applyBan(BanType.PERMANENT, null);

        // when
        boolean result = member.isBanned();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제재 상태 확인 - 유효한 제재")
    void isBanned_ValidBan_ReturnsTrue() {
        // given
        member.applyBan(BanType.BAN_1D, LocalDateTime.now().plusDays(1));

        // when
        boolean result = member.isBanned();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제재 상태 확인 - 만료된 제재")
    void isBanned_ExpiredBan_ReturnsFalse() {
        // given
        member.applyBan(BanType.BAN_1D, LocalDateTime.now().minusHours(1));

        // when
        boolean result = member.isBanned();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("게시글 작성 가능 여부 - 제재 없음")
    void canWritePost_NoBan_ReturnsTrue() {
        // when
        boolean result = member.canWritePost();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("게시글 작성 가능 여부 - 제재 중")
    void canWritePost_Banned_ReturnsFalse() {
        // given
        member.applyBan(BanType.BAN_1D, LocalDateTime.now().plusDays(1));

        // when
        boolean result = member.canWritePost();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("채팅 가능 여부 - 제재 없음")
    void canChat_NoBan_ReturnsTrue() {
        // when
        boolean result = member.canChat();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("채팅 가능 여부 - 제재 중")
    void canChat_Banned_ReturnsFalse() {
        // given
        member.applyBan(BanType.BAN_1D, LocalDateTime.now().plusDays(1));

        // when
        boolean result = member.canChat();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("매칭 가능 여부 - 제재 없음")
    void canMatch_NoBan_ReturnsTrue() {
        // when
        boolean result = member.canMatch();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("매칭 가능 여부 - 제재 중")
    void canMatch_Banned_ReturnsFalse() {
        // given
        member.applyBan(BanType.BAN_1D, LocalDateTime.now().plusDays(1));

        // when
        boolean result = member.canMatch();

        // then
        assertThat(result).isFalse();
    }
}