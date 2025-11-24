package com.gamegoo.gamegoo_v2.service.member;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberChampionRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.service.ChampionStatsRefreshService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberChampionService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.TierDetails;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotPuuidGameNameResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.Recent30GameStatsResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.AllModeStatsResponse;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotAuthService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotInfoService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotRecordService;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private RiotInfoService riotInfoService;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberRecentStatsRepository memberRecentStatsRepository;

    private Member testMemberWithPuuid;
    private Member testMemberWithoutPuuid;
    private List<ChampionStats> mockChampionStats;
    private Recent30GameStatsResponse mockRecentStats;
    private AllModeStatsResponse mockAllModeStats;
    private MemberRecentStats mockMemberRecentStats;
    private List<TierDetails> mockTierDetails;
    private RiotPuuidGameNameResponse mockAccountInfo;

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
        mockRecentStats = Recent30GameStatsResponse.builder()
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

        // Mock TierDetails
        TierDetails soloTier = mock(TierDetails.class);
        when(soloTier.getGameMode()).thenReturn(GameMode.SOLO);
        when(soloTier.getTier()).thenReturn(Tier.GOLD);
        when(soloTier.getRank()).thenReturn(3);
        when(soloTier.getWinrate()).thenReturn(65.5);
        when(soloTier.getGameCount()).thenReturn(100);

        TierDetails freeTier = mock(TierDetails.class);
        when(freeTier.getGameMode()).thenReturn(GameMode.FREE);
        when(freeTier.getTier()).thenReturn(Tier.SILVER);
        when(freeTier.getRank()).thenReturn(2);
        when(freeTier.getWinrate()).thenReturn(58.2);
        when(freeTier.getGameCount()).thenReturn(50);

        mockTierDetails = Arrays.asList(soloTier, freeTier);

        // Mock RiotPuuidGameNameResponse
        mockAccountInfo = RiotPuuidGameNameResponse.builder()
                .puuid("test-puuid-123")
                .gameName("UpdatedGameName")
                .tagLine("UpdatedTag")
                .build();

        // Mock AllModeStatsResponse
        Map<Long, ChampionStats> combinedChampionStats = new HashMap<>();
        Map<Long, ChampionStats> soloChampionStats = new HashMap<>();
        Map<Long, ChampionStats> freeChampionStats = new HashMap<>();
        Map<Long, ChampionStats> aramChampionStats = new HashMap<>();

        mockChampionStats.forEach(stats -> {
            combinedChampionStats.put(stats.getChampionId(), stats);
            soloChampionStats.put(stats.getChampionId(), stats);
        });

        mockAllModeStats = AllModeStatsResponse.builder()
                .combinedStats(mockRecentStats)
                .soloStats(mockRecentStats)
                .freeStats(mockRecentStats)
                .aramStats(mockRecentStats)
                .combinedChampionStats(combinedChampionStats)
                .soloChampionStats(soloChampionStats)
                .freeChampionStats(freeChampionStats)
                .aramChampionStats(aramChampionStats)
                .build();
    }

    @Test
    @DisplayName("증분 업데이트: 신규 매치 조회 및 DB 기반 통계 계산 성공")
    void refreshChampionStats_IncrementalUpdate_Success() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(testMemberWithPuuid);

        // 증분 업데이트: 신규 매치 5개 저장
        given(riotRecordService.fetchAndSaveNewMatches(testMemberWithPuuid, "TestUser", "test-puuid-123"))
                .willReturn(5);

        // DB에서 통계 계산 (Riot API 호출 없음)
        given(riotRecordService.getAllModeStatsFromDB(testMemberWithPuuid))
                .willReturn(mockAllModeStats);

        given(riotInfoService.getTierWinrateRank("test-puuid-123")).willReturn(mockTierDetails);
        given(memberRecentStatsRepository.findById(1L))
                .willReturn(Optional.of(mockMemberRecentStats));

        // When
        assertDoesNotThrow(() -> championStatsRefreshService.refreshChampionStats(1L));

        // Then
        verify(memberService).findMemberById(1L);
        verify(riotRecordService).fetchAndSaveNewMatches(testMemberWithPuuid, "TestUser", "test-puuid-123");
        verify(riotRecordService).getAllModeStatsFromDB(testMemberWithPuuid); // DB 기반 조회
        verify(riotInfoService).getTierWinrateRank("test-puuid-123");
        verify(testMemberWithPuuid).updateRiotBasicInfo("TestUser", "KR1");
        verify(testMemberWithPuuid).updateRiotStats(mockTierDetails);
        verify(memberChampionRepository).deleteByMember(testMemberWithPuuid);
        verify(memberChampionService).saveMemberChampions(eq(testMemberWithPuuid), anyList());
        verify(memberChampionService).saveMemberChampionsByMode(eq(testMemberWithPuuid), anyList(), anyList(), anyList());
        verify(mockMemberRecentStats).updateFrom(any(Recent30GameStatsResponse.class));
        verify(mockMemberRecentStats).updateSoloStatsFrom(any(Recent30GameStatsResponse.class));
        verify(mockMemberRecentStats).updateFreeStatsFrom(any(Recent30GameStatsResponse.class));
        verify(mockMemberRecentStats).updateAramStatsFrom(any(Recent30GameStatsResponse.class));
        verify(memberRecentStatsRepository).save(any(MemberRecentStats.class));
    }


    @Test
    @DisplayName("PUUID가 null인 경우 API로 조회 후 갱신 성공")
    void refreshChampionStats_WithNullPuuid_Success() {
        // Given
        given(memberService.findMemberById(2L)).willReturn(testMemberWithoutPuuid);
        given(riotAuthService.getPuuid("TestUser2", "KR1")).willReturn("fetched-puuid-123");

        // 증분 업데이트
        given(riotRecordService.fetchAndSaveNewMatches(testMemberWithoutPuuid, "TestUser2", "fetched-puuid-123"))
                .willReturn(5);
        given(riotRecordService.getAllModeStatsFromDB(testMemberWithoutPuuid))
                .willReturn(mockAllModeStats);

        given(riotInfoService.getTierWinrateRank("fetched-puuid-123")).willReturn(mockTierDetails);
        given(memberRecentStatsRepository.findById(2L))
                .willReturn(Optional.of(mockMemberRecentStats));

        // When
        assertDoesNotThrow(() -> championStatsRefreshService.refreshChampionStats(2L));

        // Then
        verify(memberService).findMemberById(2L);
        verify(riotAuthService).getPuuid("TestUser2", "KR1"); // null이므로 API 호출
        verify(testMemberWithoutPuuid).updatePuuid("fetched-puuid-123"); // DB에 저장
        verify(riotRecordService).fetchAndSaveNewMatches(testMemberWithoutPuuid, "TestUser2", "fetched-puuid-123");
        verify(riotRecordService).getAllModeStatsFromDB(testMemberWithoutPuuid);
        verify(riotInfoService).getTierWinrateRank("fetched-puuid-123");
        verify(memberChampionRepository).deleteByMember(testMemberWithoutPuuid);
        verify(memberChampionService).saveMemberChampions(eq(testMemberWithoutPuuid), anyList());
        verify(memberChampionService).saveMemberChampionsByMode(eq(testMemberWithoutPuuid), anyList(), anyList(), anyList());
        verify(mockMemberRecentStats).updateFrom(any(Recent30GameStatsResponse.class));
        verify(mockMemberRecentStats).updateSoloStatsFrom(any(Recent30GameStatsResponse.class));
        verify(mockMemberRecentStats).updateFreeStatsFrom(any(Recent30GameStatsResponse.class));
        verify(mockMemberRecentStats).updateAramStatsFrom(any(Recent30GameStatsResponse.class));
        verify(memberRecentStatsRepository).save(any(MemberRecentStats.class));
    }

    @Test
    @DisplayName("신규 멤버 최근 통계 생성 성공 (증분 업데이트)")
    void refreshChampionStats_NewMemberRecentStats_IncrementalUpdate_Success() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(testMemberWithPuuid);

        // 증분 업데이트
        given(riotRecordService.fetchAndSaveNewMatches(testMemberWithPuuid, "TestUser", "test-puuid-123"))
                .willReturn(10);
        given(riotRecordService.getAllModeStatsFromDB(testMemberWithPuuid))
                .willReturn(mockAllModeStats);

        given(riotInfoService.getTierWinrateRank("test-puuid-123")).willReturn(mockTierDetails);
        given(memberRecentStatsRepository.findById(1L))
                .willReturn(Optional.empty()); // 신규 멤버

        // When
        assertDoesNotThrow(() -> championStatsRefreshService.refreshChampionStats(1L));

        // Then
        verify(memberService).findMemberById(1L);
        verify(riotRecordService).fetchAndSaveNewMatches(testMemberWithPuuid, "TestUser", "test-puuid-123");
        verify(riotRecordService).getAllModeStatsFromDB(testMemberWithPuuid);
        verify(riotInfoService).getTierWinrateRank("test-puuid-123");
        verify(testMemberWithPuuid).updateRiotBasicInfo("TestUser", "KR1");
        verify(testMemberWithPuuid).updateRiotStats(mockTierDetails);
        verify(memberChampionRepository).deleteByMember(testMemberWithPuuid);
        verify(memberChampionService).saveMemberChampions(eq(testMemberWithPuuid), anyList());
        verify(memberChampionService).saveMemberChampionsByMode(eq(testMemberWithPuuid), anyList(), anyList(), anyList());
        verify(memberRecentStatsRepository).save(any(MemberRecentStats.class));
    }

    @Test
    @DisplayName("증분 업데이트 실패 시 롤백 - 기존 데이터 유지")
    void refreshChampionStats_IncrementalUpdateFails_Rollback() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(testMemberWithPuuid);

        // fetchAndSaveNewMatches에서 실패
        given(riotRecordService.fetchAndSaveNewMatches(testMemberWithPuuid, "TestUser", "test-puuid-123"))
                .willThrow(new RuntimeException("Riot API 호출 실패"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> championStatsRefreshService.refreshChampionStats(1L));

        assertTrue(exception.getMessage().contains("Riot API 호출 실패로 인한 챔피언 통계 업데이트 실패"));

        // 기존 데이터 삭제가 호출되지 않았는지 확인 (롤백)
        verify(memberChampionRepository, never()).deleteByMember(any(Member.class));
        verify(memberChampionService, never()).saveMemberChampions(any(Member.class), anyList());
        verify(memberRecentStatsRepository, never()).save(any(MemberRecentStats.class));
    }

    @Test
    @DisplayName("데이터 저장 중 실패 시 트랜잭션 롤백 (증분 업데이트)")
    void refreshChampionStats_SaveFails_TransactionRollback() {
        // Given
        given(memberService.findMemberById(1L)).willReturn(testMemberWithPuuid);

        // 증분 업데이트 성공
        given(riotRecordService.fetchAndSaveNewMatches(testMemberWithPuuid, "TestUser", "test-puuid-123"))
                .willReturn(5);
        given(riotRecordService.getAllModeStatsFromDB(testMemberWithPuuid))
                .willReturn(mockAllModeStats);

        given(riotInfoService.getTierWinrateRank("test-puuid-123")).willReturn(mockTierDetails);
        given(memberRecentStatsRepository.findById(1L))
                .willReturn(Optional.of(mockMemberRecentStats));

        // 저장 시 실패
        doThrow(new RuntimeException("DB 저장 실패"))
                .when(memberRecentStatsRepository).save(any(MemberRecentStats.class));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> championStatsRefreshService.refreshChampionStats(1L));

        assertTrue(exception.getMessage().contains("Riot API 호출 실패로 인한 챔피언 통계 업데이트 실패"));

        // API 호출은 성공했지만 저장에서 실패한 경우
        verify(memberChampionRepository).deleteByMember(testMemberWithPuuid);
        verify(memberChampionService).saveMemberChampions(eq(testMemberWithPuuid), anyList());
        verify(memberChampionService).saveMemberChampionsByMode(eq(testMemberWithPuuid), anyList(), anyList(), anyList());
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
