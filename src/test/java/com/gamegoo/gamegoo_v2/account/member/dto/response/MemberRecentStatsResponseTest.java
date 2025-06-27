package com.gamegoo.gamegoo_v2.account.member.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MemberRecentStatsResponseTest {

    @DisplayName("MemberRecentStats가 null이면 null을 반환한다")
    @Test
    void from_WithNullMemberRecentStats_ReturnsNull() {
        // when
        MemberRecentStatsResponse response = MemberRecentStatsResponse.from(null);

        // then
        assertThat(response).isNull();
    }

    @DisplayName("MemberRecentStats로부터 MemberRecentStatsResponse를 생성한다")
    @Test
    void from_WithValidMemberRecentStats_ReturnsResponse() {
        // given
        Member mockMember = mock(Member.class);
        MemberRecentStats recentStats = MemberRecentStats.builder()
            .memberId(1L)
            .member(mockMember)
            .recTotalWins(25)
            .recTotalLosses(15)
            .recWinRate(62.5)
            .recAvgKDA(2.5)
            .recAvgCsPerMinute(6.8)
            .recTotalCs(1360)
            .build();

        // when
        MemberRecentStatsResponse response = MemberRecentStatsResponse.from(recentStats);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecTotalWins()).isEqualTo(25);
        assertThat(response.getRecTotalLosses()).isEqualTo(15);
        assertThat(response.getRecWinRate()).isEqualTo(62.5);
        assertThat(response.getRecAvgKDA()).isEqualTo(2.5);
        assertThat(response.getRecAvgCsPerMinute()).isEqualTo(6.8);
        assertThat(response.getRecTotalCs()).isEqualTo(1360);
    }

    @DisplayName("모든 필드가 0인 경우도 정상적으로 변환된다")
    @Test
    void from_WithZeroValues_ReturnsResponseWithZeroValues() {
        // given
        Member mockMember = mock(Member.class);
        MemberRecentStats recentStats = MemberRecentStats.builder()
            .memberId(1L)
            .member(mockMember)
            .recTotalWins(0)
            .recTotalLosses(0)
            .recWinRate(0.0)
            .recAvgKDA(0.0)
            .recAvgCsPerMinute(0.0)
            .recTotalCs(0)
            .build();

        // when
        MemberRecentStatsResponse response = MemberRecentStatsResponse.from(recentStats);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecTotalWins()).isEqualTo(0);
        assertThat(response.getRecTotalLosses()).isEqualTo(0);
        assertThat(response.getRecWinRate()).isEqualTo(0.0);
        assertThat(response.getRecAvgKDA()).isEqualTo(0.0);
        assertThat(response.getRecAvgCsPerMinute()).isEqualTo(0.0);
        assertThat(response.getRecTotalCs()).isEqualTo(0);
    }
}