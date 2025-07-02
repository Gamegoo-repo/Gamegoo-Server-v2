package com.gamegoo.gamegoo_v2.service.member;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.BanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("BanService 테스트")
class BanServiceTest {

    @InjectMocks
    private BanService banService;

    private Member member;

    @BeforeEach
    void setUp() {
        // Mock Member 객체 생성 (실제 Member.createForGeneral 사용)
        member = Member.createForGeneral(
                "test@example.com",
                "password",
                com.gamegoo.gamegoo_v2.account.member.domain.LoginType.GENERAL,
                "testUser",
                "KR1",
                com.gamegoo.gamegoo_v2.account.member.domain.Tier.BRONZE,
                1,
                50.0,
                com.gamegoo.gamegoo_v2.account.member.domain.Tier.BRONZE,
                1,
                50.0,
                10,
                5,
                true
        );
    }

    @Test
    @DisplayName("1일 제재 적용 테스트")
    void applyBan_1Day_Success() {
        // when
        banService.applyBan(member, BanType.BAN_1D);

        // then
        assertThat(member.getBanType()).isEqualTo(BanType.BAN_1D);
        assertThat(member.getBanExpireAt()).isNotNull();
        assertThat(member.getBanExpireAt()).isAfter(LocalDateTime.now());
        assertThat(member.getBanExpireAt()).isBefore(LocalDateTime.now().plusDays(2));
    }

    @Test
    @DisplayName("영구 제재 적용 테스트")
    void applyBan_Permanent_Success() {
        // when
        banService.applyBan(member, BanType.PERMANENT);

        // then
        assertThat(member.getBanType()).isEqualTo(BanType.PERMANENT);
        assertThat(member.getBanExpireAt()).isNull();
    }

    @Test
    @DisplayName("경고 적용 테스트")
    void applyBan_Warning_Success() {
        // when
        banService.applyBan(member, BanType.WARNING);

        // then
        assertThat(member.getBanType()).isEqualTo(BanType.WARNING);
        assertThat(member.getBanExpireAt()).isNull();
    }

    @Test
    @DisplayName("제재 해제 테스트")
    void releaseBan_Success() {
        // given
        member.applyBan(BanType.BAN_1D, LocalDateTime.now().plusDays(1));

        // when
        banService.releaseBan(member);

        // then
        assertThat(member.getBanType()).isEqualTo(BanType.NONE);
        assertThat(member.getBanExpireAt()).isNull();
    }

    @Test
    @DisplayName("제재 상태 확인 - 제재 중")
    void isBanned_True() {
        // given
        member.applyBan(BanType.BAN_1D, LocalDateTime.now().plusDays(1));

        // when
        boolean result = banService.isBanned(member);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제재 상태 확인 - 제재 없음")
    void isBanned_False() {
        // when
        boolean result = banService.isBanned(member);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("제재 만료 체크 - 만료된 제재 자동 해제")
    void checkBanExpiry_ExpiredBan_AutoRelease() {
        // given
        member.applyBan(BanType.BAN_1D, LocalDateTime.now().minusHours(1)); // 이미 만료

        // when
        banService.checkBanExpiry(member);

        // then
        assertThat(member.getBanType()).isEqualTo(BanType.NONE);
        assertThat(member.getBanExpireAt()).isNull();
    }

    @Test
    @DisplayName("제재 만료 체크 - 아직 유효한 제재")
    void checkBanExpiry_ValidBan_NoChange() {
        // given
        LocalDateTime expireAt = LocalDateTime.now().plusDays(1);
        member.applyBan(BanType.BAN_1D, expireAt);

        // when
        banService.checkBanExpiry(member);

        // then
        assertThat(member.getBanType()).isEqualTo(BanType.BAN_1D);
        assertThat(member.getBanExpireAt()).isEqualTo(expireAt);
    }

}