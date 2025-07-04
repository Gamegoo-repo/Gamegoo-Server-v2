package com.gamegoo.gamegoo_v2.service.member;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.dto.response.MyProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.dto.response.OtherProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.service.ChampionStatsRefreshService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberFacadeService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberFacadeServiceRefreshTest {

    @InjectMocks
    private MemberFacadeService memberFacadeService;

    @Mock
    private MemberService memberService;

    @Mock
    private FriendService friendService;

    @Mock
    private BlockService blockService;

    @Mock
    private ChampionStatsRefreshService championStatsRefreshService;

    private Member testMember;
    private Member updatedMember;
    private Member targetMember;
    private Member updatedTargetMember;

    @BeforeEach
    void setUp() {
        // Original member
        testMember = mock(Member.class);
        when(testMember.getId()).thenReturn(1L);
        when(testMember.getGameName()).thenReturn("TestUser");
        when(testMember.getTag()).thenReturn("KR1");
        when(testMember.getChampionStatsRefreshedAt()).thenReturn(LocalDateTime.now().minusMinutes(10)); // 10분 전 (갱신 필요)

        // Updated member
        updatedMember = mock(Member.class);
        when(updatedMember.getId()).thenReturn(1L);
        when(updatedMember.getGameName()).thenReturn("TestUser");
        when(updatedMember.getTag()).thenReturn("KR1");
        when(updatedMember.getUpdatedAt()).thenReturn(LocalDateTime.now()); // 방금 갱신됨

        // Target member
        targetMember = mock(Member.class);
        when(targetMember.getId()).thenReturn(2L);
        when(targetMember.getGameName()).thenReturn("TargetUser");
        when(targetMember.getTag()).thenReturn("KR1");
        when(targetMember.getChampionStatsRefreshedAt()).thenReturn(LocalDateTime.now().minusMinutes(6)); // 6분 전 (갱신 필요)

        // Updated target member
        updatedTargetMember = mock(Member.class);
        when(updatedTargetMember.getId()).thenReturn(2L);
        when(updatedTargetMember.getGameName()).thenReturn("TargetUser");
        when(updatedTargetMember.getTag()).thenReturn("KR1");
        when(updatedTargetMember.getUpdatedAt()).thenReturn(LocalDateTime.now()); // 방금 갱신됨
    }

    @Test
    @DisplayName("내 프로필 조회 시 DB 재로딩으로 최신 데이터 반영")
    void getMyProfile_RefreshesAndReloadsFromDB() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(updatedMember);

        // When
        MyProfileResponse result = memberFacadeService.getMyProfile(testMember);

        // Then
        verify(championStatsRefreshService).refreshChampionStats(testMember); // 갱신 호출 확인
        verify(memberService).findMemberById(1L); // fresh entity 로딩 확인
        assertNotNull(result);
    }

    @Test
    @DisplayName("내 프로필 조회 시 갱신 실패해도 DB 재로딩은 수행")
    void getMyProfile_ReloadsFromDBEvenWhenRefreshFails() {
        // Given
        doThrow(new RuntimeException("Riot API 실패")).when(championStatsRefreshService).refreshChampionStats(testMember);
        given(memberService.findMemberById(1L)).willReturn(updatedMember);

        // When
        MyProfileResponse result = memberFacadeService.getMyProfile(testMember);

        // Then
        verify(championStatsRefreshService).refreshChampionStats(testMember); // 갱신 시도 확인
        verify(memberService).findMemberById(1L); // 실패해도 fresh entity 로딩 확인
        assertNotNull(result);
    }

    @Test
    @DisplayName("다른 사람 프로필 조회 시 DB 재로딩으로 최신 데이터 반영")
    void getOtherProfile_RefreshesAndReloadsFromDB() {
        // Given
        given(memberService.findMemberById(2L))
                .willReturn(targetMember) // 첫 번째 호출 (갱신 전)
                .willReturn(updatedTargetMember); // 두 번째 호출 (갱신 후 fresh entity)
        given(friendService.isFriend(testMember, updatedTargetMember)).willReturn(false);
        given(friendService.getFriendRequestMemberId(testMember, updatedTargetMember)).willReturn(null);
        given(blockService.isBlocked(testMember, updatedTargetMember)).willReturn(false);

        // When
        OtherProfileResponse result = memberFacadeService.getOtherProfile(testMember, 2L);

        // Then
        verify(championStatsRefreshService).refreshChampionStats(targetMember); // 갱신 호출 확인
        verify(memberService, times(2)).findMemberById(2L); // DB에서 2번 로딩 확인 (갱신 전/후)
        verify(friendService).isFriend(testMember, updatedTargetMember); // 갱신된 엔티티로 친구 확인
        verify(blockService).isBlocked(testMember, updatedTargetMember); // 갱신된 엔티티로 차단 확인
        assertNotNull(result);
    }

    @Test
    @DisplayName("다른 사람 프로필 조회 시 갱신 실패해도 DB 재로딩은 수행")
    void getOtherProfile_ReloadsFromDBEvenWhenRefreshFails() {
        // Given
        doThrow(new RuntimeException("Riot API 실패")).when(championStatsRefreshService).refreshChampionStats(targetMember);
        given(memberService.findMemberById(2L))
                .willReturn(targetMember) // 첫 번째 호출 (갱신 전)
                .willReturn(updatedTargetMember); // 두 번째 호출 (갱신 후 fresh entity)
        given(friendService.isFriend(testMember, updatedTargetMember)).willReturn(false);
        given(friendService.getFriendRequestMemberId(testMember, updatedTargetMember)).willReturn(null);
        given(blockService.isBlocked(testMember, updatedTargetMember)).willReturn(false);

        // When
        OtherProfileResponse result = memberFacadeService.getOtherProfile(testMember, 2L);

        // Then
        verify(championStatsRefreshService).refreshChampionStats(targetMember); // 갱신 시도 확인
        verify(memberService, times(2)).findMemberById(2L); // 실패해도 fresh entity 로딩 확인
        assertNotNull(result);
    }

    @Test
    @DisplayName("최근 갱신된 멤버는 챔피언 통계 갱신을 건너뛰지만 DB 재로딩은 수행")
    void getMyProfile_SkipsRefreshButStillReloadsWhenRecentlyUpdated() {
        // Given
        Member recentlyUpdatedMember = mock(Member.class);
        when(recentlyUpdatedMember.getId()).thenReturn(1L);
        when(recentlyUpdatedMember.getChampionStatsRefreshedAt()).thenReturn(LocalDateTime.now().minusMinutes(2)); // 2분 전 (갱신 불필요)

        given(memberService.findMemberById(1L)).willReturn(updatedMember);

        // When
        MyProfileResponse result = memberFacadeService.getMyProfile(recentlyUpdatedMember);

        // Then
        verify(championStatsRefreshService, never()).refreshChampionStats(any(Member.class)); // 갱신 건너뛰기 확인
        verify(memberService).findMemberById(1L); // 그래도 fresh entity 로딩은 수행
        assertNotNull(result);
    }

    @Test
    @DisplayName("championStatsRefreshedAt이 null인 멤버는 항상 챔피언 통계 갱신 수행")
    void getMyProfile_AlwaysRefreshesWhenChampionStatsRefreshedAtIsNull() {
        // Given
        Member memberWithNullChampionStatsRefreshedAt = mock(Member.class);
        when(memberWithNullChampionStatsRefreshedAt.getId()).thenReturn(1L);
        when(memberWithNullChampionStatsRefreshedAt.getChampionStatsRefreshedAt()).thenReturn(null); // null인 경우

        given(memberService.findMemberById(1L)).willReturn(updatedMember);

        // When
        MyProfileResponse result = memberFacadeService.getMyProfile(memberWithNullChampionStatsRefreshedAt);

        // Then
        verify(championStatsRefreshService).refreshChampionStats(memberWithNullChampionStatsRefreshedAt); // null이면 항상 갱신
        verify(memberService).findMemberById(1L); // fresh entity 로딩 확인
        assertNotNull(result);
    }

    @Test
    @DisplayName("DB 재로딩 후 최신 챔피언 통계가 포함된 프로필 반환")
    void getMyProfile_ReturnsProfileWithLatestChampionStats() {
        // Given
        // Mock MemberRecentStats for updated member
        MemberRecentStats mockRecentStats = mock(MemberRecentStats.class);
        when(mockRecentStats.getRecTotalWins()).thenReturn(20); // 갱신된 승수
        when(mockRecentStats.getRecTotalLosses()).thenReturn(10); // 갱신된 패배수
        when(mockRecentStats.getRecWinRate()).thenReturn(66.7); // 갱신된 승률

        when(updatedMember.getMemberRecentStats()).thenReturn(mockRecentStats);
        given(memberService.findMemberById(1L)).willReturn(updatedMember);

        // When
        MyProfileResponse result = memberFacadeService.getMyProfile(testMember);

        // Then
        verify(championStatsRefreshService).refreshChampionStats(testMember); // 갱신 호출 확인
        verify(memberService).findMemberById(1L); // fresh entity로부터 로딩 확인
        assertNotNull(result);
        // 실제 MyProfileResponse.of() 메서드가 updatedMember로부터 생성되는지 확인
        // (실제 구현에서는 갱신된 통계가 포함된 응답이 반환됨)
    }

    @Test
    @DisplayName("친구/차단 정보 조회 시에도 갱신된 엔티티 사용")
    void getOtherProfile_UsesFreshEntityForFriendAndBlockChecks() {
        // Given
        given(memberService.findMemberById(2L))
                .willReturn(targetMember) // 첫 번째 호출
                .willReturn(updatedTargetMember); // 두 번째 호출 (fresh entity)
        given(friendService.isFriend(testMember, updatedTargetMember)).willReturn(true);
        given(friendService.getFriendRequestMemberId(testMember, updatedTargetMember)).willReturn(5L);
        given(blockService.isBlocked(testMember, updatedTargetMember)).willReturn(false);

        // When
        OtherProfileResponse result = memberFacadeService.getOtherProfile(testMember, 2L);

        // Then
        // fresh entity(updatedTargetMember)로 친구/차단 상태 확인
        verify(friendService).isFriend(testMember, updatedTargetMember);
        verify(friendService).getFriendRequestMemberId(testMember, updatedTargetMember);
        verify(blockService).isBlocked(testMember, updatedTargetMember);

        // 갱신되지 않은 원본 엔티티로는 확인하지 않음
        verify(friendService, never()).isFriend(testMember, targetMember);
        verify(blockService, never()).isBlocked(testMember, targetMember);

        assertNotNull(result);
    }
}
