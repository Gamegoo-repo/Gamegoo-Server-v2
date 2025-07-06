package com.gamegoo.gamegoo_v2.service.member;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberChampionRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.service.ChampionStatsRefreshService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberChampionService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotAuthService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChampionStatsRefreshServiceTest {

    @InjectMocks
    private ChampionStatsRefreshService championStatsRefreshService;

    @Mock
    private RiotAuthService riotAuthService;

    @Mock
    private MemberChampionService memberChampionService;

    @Mock
    private MemberChampionRepository memberChampionRepository;

    @Mock
    private RiotRecordService riotRecordService;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberRecentStatsRepository memberRecentStatsRepository;

    private Member testMemberWithPuuid;
    private Member testMemberWithoutPuuid;
    private List<ChampionStats> mockChampionStats;
    private RiotRecordService.Recent30GameStatsResponse mockRecentStats;
    private MemberRecentStats mockMemberRecentStats;

    @BeforeEach
    void setUp() {
        // Member with puuid
        testMemberWithPuuid = mock(Member.class);
        when(testMemberWithPuuid.getId()).thenReturn(1L);
        when(testMemberWithPuuid.getGameName()).thenReturn("TestUser");
        when(testMemberWithPuuid.getTag()).thenReturn("KR1");
        when(testMemberWithPuuid.getPuuid()).thenReturn("test-puuid-123");

        // Member without puuid
        testMemberWithoutPuuid = mock(Member.class);
        when(testMemberWithoutPuuid.getId()).thenReturn(2L);
        when(testMemberWithoutPuuid.getGameName()).thenReturn("TestUser2");
        when(testMemberWithoutPuuid.getTag()).thenReturn("KR1");
        when(testMemberWithoutPuuid.getPuuid()).thenReturn(null);

        // Mock ChampionStats
        mockChampionStats = Arrays.asList(
                createChampionStats(1L, true, 10, 5, 3, 150),
                createChampionStats(2L, false, 8, 7, 12, 120)
        );

        // Mock Recent30GameStatsResponse
        mockRecentStats = RiotRecordService.Recent30GameStatsResponse.builder()
                .recTotalWins(18)
                .recTotalLosses(12)
                .recWinRate(60.0)
                .recAvgKDA(2.5)
                .recAvgKills(8.5)
                .recAvgDeaths(5.2)
                .recAvgAssists(10.3)
                .recAvgCsPerMinute(6.5)
                .recTotalCs(150)
                .build();

        // Mock MemberRecentStats
        mockMemberRecentStats = mock(MemberRecentStats.class);
    }

    @Test
    @DisplayName("puuid가 있는 멤버의 챔피언 통계 갱신 성공 - API 호출 없이 캐시된 puuid 사용")
    void refreshChampionStats_WithPuuid_Success() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(testMemberWithPuuid);
        given(riotRecordService.getPreferChampionfromMatch("TestUser", "test-puuid-123"))
                .willReturn(mockChampionStats);
        given(riotRecordService.getRecent30GameStats("TestUser", "test-puuid-123"))
                .willReturn(mockRecentStats);
        given(memberRecentStatsRepository.findById(1L))
                .willReturn(Optional.of(mockMemberRecentStats));

        // When
        assertDoesNotThrow(() -> championStatsRefreshService.refreshChampionStats(testMemberWithPuuid));

        // Then
        verify(memberService).findMemberById(1L);
        verify(riotAuthService, never()).getPuuid(anyString(), anyString()); // puuid 캐시 확인
        verify(riotRecordService).getPreferChampionfromMatch("TestUser", "test-puuid-123");
        verify(riotRecordService).getRecent30GameStats("TestUser", "test-puuid-123");
        verify(memberChampionRepository).deleteByMember(testMemberWithPuuid);
        verify(memberChampionService).saveMemberChampions(testMemberWithPuuid, mockChampionStats);
        verify(memberRecentStatsRepository).save(any(MemberRecentStats.class));
    }

    @Test
    @DisplayName("puuid가 없는 멤버의 챔피언 통계 갱신 성공 - API 호출로 puuid 조회")
    void refreshChampionStats_WithoutPuuid_Success() {
        // Given
        given(memberService.findMemberById(2L)).willReturn(testMemberWithoutPuuid);
        given(riotAuthService.getPuuid("TestUser2", "KR1")).willReturn("fetched-puuid-123");
        given(riotRecordService.getPreferChampionfromMatch("TestUser2", "fetched-puuid-123"))
                .willReturn(mockChampionStats);
        given(riotRecordService.getRecent30GameStats("TestUser2", "fetched-puuid-123"))
                .willReturn(mockRecentStats);
        given(memberRecentStatsRepository.findById(2L))
                .willReturn(Optional.of(mockMemberRecentStats));

        // When
        assertDoesNotThrow(() -> championStatsRefreshService.refreshChampionStats(testMemberWithoutPuuid));

        // Then
        verify(memberService).findMemberById(2L);
        verify(riotAuthService).getPuuid("TestUser2", "KR1");
        verify(riotRecordService).getPreferChampionfromMatch("TestUser2", "fetched-puuid-123");
        verify(riotRecordService).getRecent30GameStats("TestUser2", "fetched-puuid-123");
        verify(memberChampionRepository).deleteByMember(testMemberWithoutPuuid);
        verify(memberChampionService).saveMemberChampions(testMemberWithoutPuuid, mockChampionStats);
        verify(memberRecentStatsRepository).save(any(MemberRecentStats.class));
    }

    @Test
    @DisplayName("신규 멤버 최근 통계 생성 성공")
    void refreshChampionStats_NewMemberRecentStats_Success() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(testMemberWithPuuid);
        given(riotRecordService.getPreferChampionfromMatch("TestUser", "test-puuid-123"))
                .willReturn(mockChampionStats);
        given(riotRecordService.getRecent30GameStats("TestUser", "test-puuid-123"))
                .willReturn(mockRecentStats);
        given(memberRecentStatsRepository.findById(1L))
                .willReturn(Optional.empty()); // 신규 멤버

        // When
        assertDoesNotThrow(() -> championStatsRefreshService.refreshChampionStats(testMemberWithPuuid));

        // Then
        verify(memberService).findMemberById(1L);
        verify(riotRecordService).getPreferChampionfromMatch("TestUser", "test-puuid-123");
        verify(riotRecordService).getRecent30GameStats("TestUser", "test-puuid-123");
        verify(memberChampionRepository).deleteByMember(testMemberWithPuuid);
        verify(memberChampionService).saveMemberChampions(testMemberWithPuuid, mockChampionStats);
        verify(memberRecentStatsRepository).save(any(MemberRecentStats.class));
    }

    @Test
    @DisplayName("선호 챔피언 조회 실패 시 롤백 - 기존 데이터 유지")
    void refreshChampionStats_PreferChampionsFetchFails_Rollback() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(testMemberWithPuuid);
        given(riotRecordService.getPreferChampionfromMatch("TestUser", "test-puuid-123"))
                .willThrow(new RuntimeException("Riot API 호출 실패"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> championStatsRefreshService.refreshChampionStats(testMemberWithPuuid));
        
        assertTrue(exception.getMessage().contains("Riot API 호출 실패로 인한 챔피언 통계 업데이트 실패"));
        
        // 기존 데이터 삭제가 호출되지 않았는지 확인 (롤백)
        verify(memberChampionRepository, never()).deleteByMember(any(Member.class));
        verify(memberChampionService, never()).saveMemberChampions(any(Member.class), anyList());
        verify(memberRecentStatsRepository, never()).save(any(MemberRecentStats.class));
    }

    @Test
    @DisplayName("최근 30게임 통계 조회 실패 시 롤백 - 기존 데이터 유지")
    void refreshChampionStats_RecentStatsFetchFails_Rollback() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(testMemberWithPuuid);
        given(riotRecordService.getPreferChampionfromMatch("TestUser", "test-puuid-123"))
                .willReturn(mockChampionStats);
        given(riotRecordService.getRecent30GameStats("TestUser", "test-puuid-123"))
                .willThrow(new RuntimeException("Riot API 호출 실패"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> championStatsRefreshService.refreshChampionStats(testMemberWithPuuid));
        
        assertTrue(exception.getMessage().contains("Riot API 호출 실패로 인한 챔피언 통계 업데이트 실패"));
        
        // 기존 데이터 삭제가 호출되지 않았는지 확인 (롤백)
        verify(memberChampionRepository, never()).deleteByMember(any(Member.class));
        verify(memberChampionService, never()).saveMemberChampions(any(Member.class), anyList());
        verify(memberRecentStatsRepository, never()).save(any(MemberRecentStats.class));
    }

    @Test
    @DisplayName("puuid 조회 실패 시 예외 발생")
    void refreshChampionStats_PuuidFetchFails_ThrowsException() {
        // Given
        given(memberService.findMemberById(2L)).willReturn(testMemberWithoutPuuid);
        given(riotAuthService.getPuuid("TestUser2", "KR1"))
                .willThrow(new RuntimeException("Riot API 호출 실패"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> championStatsRefreshService.refreshChampionStats(testMemberWithoutPuuid));
        
        assertNotNull(exception.getMessage());
        
        // 기존 데이터 삭제가 호출되지 않았는지 확인 (롤백)
        verify(memberChampionRepository, never()).deleteByMember(any(Member.class));
        verify(memberChampionService, never()).saveMemberChampions(any(Member.class), anyList());
        verify(memberRecentStatsRepository, never()).save(any(MemberRecentStats.class));
    }

    @Test
    @DisplayName("데이터 저장 중 실패 시 트랜잭션 롤백")
    void refreshChampionStats_SaveFails_TransactionRollback() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(testMemberWithPuuid);
        given(riotRecordService.getPreferChampionfromMatch("TestUser", "test-puuid-123"))
                .willReturn(mockChampionStats);
        given(riotRecordService.getRecent30GameStats("TestUser", "test-puuid-123"))
                .willReturn(mockRecentStats);
        given(memberRecentStatsRepository.findById(1L))
                .willReturn(Optional.of(mockMemberRecentStats));
        doThrow(new RuntimeException("DB 저장 실패"))
                .when(memberRecentStatsRepository).save(any(MemberRecentStats.class));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> championStatsRefreshService.refreshChampionStats(testMemberWithPuuid));
        
        assertTrue(exception.getMessage().contains("Riot API 호출 실패로 인한 챔피언 통계 업데이트 실패"));
        
        // API 호출은 성공했지만 저장에서 실패한 경우
        verify(memberChampionRepository).deleteByMember(testMemberWithPuuid);
        verify(memberChampionService).saveMemberChampions(testMemberWithPuuid, mockChampionStats);
        verify(memberRecentStatsRepository).save(any(MemberRecentStats.class));
    }

    private ChampionStats createChampionStats(Long championId, boolean win, int kills, int deaths, int assists, int cs) {
        ChampionStats stats = new ChampionStats(championId, win);
        stats.setKills(kills);
        stats.setDeaths(deaths);
        stats.setAssists(assists);
        stats.setTotalMinionsKilled(cs);
        stats.setGameTime(1800); // 30분
        return stats;
    }
}