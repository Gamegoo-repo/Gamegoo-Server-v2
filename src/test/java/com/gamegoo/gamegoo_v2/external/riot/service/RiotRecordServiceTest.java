package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotMatchResponse;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotRecordService.Recent30GameStatsResponse;
import com.gamegoo.gamegoo_v2.utils.RiotApiHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiotRecordServiceTest {

    @InjectMocks
    private RiotRecordService riotRecordService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RiotApiHelper riotApiHelper;

    @Test
    @DisplayName("매치 데이터에서 KDA 정보를 정상적으로 가져온다")
    void getKDAFromMatchData() {
        // given
        String puuid = "test-puuid";
        String gameName = "testUser";
        String matchId = "test-match-id";
        
        // API 키 설정
        ReflectionTestUtils.setField(riotRecordService, "riotAPIKey", "test-api-key");

        // 매치 ID 목록 응답 (한 번만 반환)
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(new String[]{matchId})
                .thenReturn(new String[]{}); // 두 번째 호출부터는 빈 배열 반환

        // 매치 상세 정보 응답 (한 번만 반환)
        RiotMatchResponse.ParticipantDTO participant = RiotMatchResponse.ParticipantDTO.builder()
                .riotIdGameName(gameName)
                .championId(1L)
                .win(true)
                .kills(10)
                .deaths(2)
                .assists(15)
                .totalMinionsKilled(200)
                .neutralMinionsKilled(30)
                .build();

        RiotMatchResponse.InfoDTO info = RiotMatchResponse.InfoDTO.builder()
                .participants(Arrays.asList(participant))
                .gameDuration(1800) // 30분
                .queueId(420)
                .build();

        RiotMatchResponse matchResponse = RiotMatchResponse.builder()
                .info(info)
                .build();

        when(restTemplate.getForObject(any(String.class), eq(RiotMatchResponse.class)))
                .thenReturn(matchResponse);

        // when
        List<ChampionStats> result = riotRecordService.getPreferChampionfromMatch(gameName, puuid);

        // then
        assertThat(result).isNotEmpty();
        ChampionStats stats = result.get(0);
        assertThat(stats.getKills()).isEqualTo(10);
        assertThat(stats.getDeaths()).isEqualTo(2);
        assertThat(stats.getAssists()).isEqualTo(15);
        assertThat(stats.getKDA()).isEqualTo(12.5); // (10 + 15) / 2
    }

    @Test
    @DisplayName("데스가 0인 경우 KDA는 킬과 어시스트의 합이다")
    void calculateKDAWhenNoDeaths() {
        // given
        String puuid = "test-puuid";
        String gameName = "testUser";
        String matchId = "test-match-id";
        
        ReflectionTestUtils.setField(riotRecordService, "riotAPIKey", "test-api-key");

        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(new String[]{matchId})
                .thenReturn(new String[]{});

        RiotMatchResponse.ParticipantDTO participant = RiotMatchResponse.ParticipantDTO.builder()
                .riotIdGameName(gameName)
                .championId(1L)
                .win(true)
                .kills(5)
                .deaths(0)
                .assists(10)
                .totalMinionsKilled(150)
                .neutralMinionsKilled(25)
                .build();

        RiotMatchResponse.InfoDTO info = RiotMatchResponse.InfoDTO.builder()
                .participants(Arrays.asList(participant))
                .gameDuration(1800)
                .queueId(420)
                .build();

        RiotMatchResponse matchResponse = RiotMatchResponse.builder()
                .info(info)
                .build();

        when(restTemplate.getForObject(any(String.class), eq(RiotMatchResponse.class)))
                .thenReturn(matchResponse);

        // when
        List<ChampionStats> result = riotRecordService.getPreferChampionfromMatch(gameName, puuid);

        // then
        assertThat(result).isNotEmpty();
        ChampionStats stats = result.get(0);
        assertThat(stats.getKills()).isEqualTo(5);
        assertThat(stats.getDeaths()).isEqualTo(0);
        assertThat(stats.getAssists()).isEqualTo(10);
        assertThat(stats.getKDA()).isEqualTo(15.0); // 5 + 10
    }

    @Test
    @DisplayName("여러 게임의 KDA가 정상적으로 누적된다")
    void accumulateKDAFromMultipleGames() {
        // given
        String puuid = "test-puuid";
        String gameName = "testUser";
        String[] matchIds = {"match-1", "match-2"};
        
        ReflectionTestUtils.setField(riotRecordService, "riotAPIKey", "test-api-key");

        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(matchIds)
                .thenReturn(new String[]{});

        // 첫 번째 게임
        RiotMatchResponse.ParticipantDTO participant1 = RiotMatchResponse.ParticipantDTO.builder()
                .riotIdGameName(gameName)
                .championId(1L)
                .win(true)
                .kills(8)
                .deaths(3)
                .assists(12)
                .totalMinionsKilled(180)
                .neutralMinionsKilled(20)
                .build();

        RiotMatchResponse.InfoDTO info1 = RiotMatchResponse.InfoDTO.builder()
                .participants(Arrays.asList(participant1))
                .gameDuration(1800)
                .queueId(420)
                .build();

        RiotMatchResponse match1 = RiotMatchResponse.builder()
                .info(info1)
                .build();

        // 두 번째 게임
        RiotMatchResponse.ParticipantDTO participant2 = RiotMatchResponse.ParticipantDTO.builder()
                .riotIdGameName(gameName)
                .championId(1L)
                .win(true)
                .kills(6)
                .deaths(2)
                .assists(10)
                .totalMinionsKilled(160)
                .neutralMinionsKilled(15)
                .build();

        RiotMatchResponse.InfoDTO info2 = RiotMatchResponse.InfoDTO.builder()
                .participants(Arrays.asList(participant2))
                .gameDuration(1800)
                .queueId(420)
                .build();

        RiotMatchResponse match2 = RiotMatchResponse.builder()
                .info(info2)
                .build();

        when(restTemplate.getForObject(any(String.class), eq(RiotMatchResponse.class)))
                .thenReturn(match1)
                .thenReturn(match2);

        // when
        List<ChampionStats> result = riotRecordService.getPreferChampionfromMatch(gameName, puuid);

        // then
        assertThat(result).isNotEmpty();
        ChampionStats stats = result.get(0);
        assertThat(stats.getKills()).isEqualTo(14); // 8 + 6
        assertThat(stats.getDeaths()).isEqualTo(5); // 3 + 2
        assertThat(stats.getAssists()).isEqualTo(22); // 12 + 10
        assertThat(stats.getKDA()).isEqualTo(7.2); // (14 + 22) / 5
    }

    @Test
    @DisplayName("최근 30게임 통계에서 평균 KDA 개별값이 double로 정확히 계산된다")
    void calculateRecent30GameStatsWithDoubleAverage() {
        // given
        String puuid = "test-puuid";
        String gameName = "testUser";
        String[] matchIds = {"match-1", "match-2", "match-3"};
        
        ReflectionTestUtils.setField(riotRecordService, "riotAPIKey", "test-api-key");

        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(matchIds);

        // 첫 번째 게임: 11킬, 3데스, 7어시스트
        RiotMatchResponse.ParticipantDTO participant1 = createParticipant(gameName, 11, 3, 7, true);
        RiotMatchResponse match1 = createMatchResponse(participant1);
        
        // 두 번째 게임: 5킬, 4데스, 8어시스트  
        RiotMatchResponse.ParticipantDTO participant2 = createParticipant(gameName, 5, 4, 8, false);
        RiotMatchResponse match2 = createMatchResponse(participant2);
        
        // 세 번째 게임: 8킬, 2데스, 12어시스트
        RiotMatchResponse.ParticipantDTO participant3 = createParticipant(gameName, 8, 2, 12, true);
        RiotMatchResponse match3 = createMatchResponse(participant3);

        when(restTemplate.getForObject(any(String.class), eq(RiotMatchResponse.class)))
                .thenReturn(match1)
                .thenReturn(match2)
                .thenReturn(match3);

        // when
        Recent30GameStatsResponse result = riotRecordService.getRecent30GameStats(gameName, puuid);

        // then
        assertThat(result).isNotNull();
        
        // 총합: 24킬, 9데스, 27어시스트, 3게임
        // 평균: 8.0킬, 3.0데스, 9.0어시스트
        assertThat(result.getRecAvgKills()).isEqualTo(8.0); // 24 / 3 = 8.0
        assertThat(result.getRecAvgDeaths()).isEqualTo(3.0); // 9 / 3 = 3.0
        assertThat(result.getRecAvgAssists()).isEqualTo(9.0); // 27 / 3 = 9.0
        
        // 소수점 계산 확인을 위한 추가 테스트
        assertThat(result.getRecAvgKills()).isInstanceOf(Double.class);
        assertThat(result.getRecAvgDeaths()).isInstanceOf(Double.class);
        assertThat(result.getRecAvgAssists()).isInstanceOf(Double.class);
    }

    @Test
    @DisplayName("최근 30게임 통계에서 소수점이 있는 평균 KDA가 정확히 계산된다")
    void calculateRecent30GameStatsWithDecimalAverage() {
        // given
        String puuid = "test-puuid";
        String gameName = "testUser";
        String[] matchIds = {"match-1", "match-2"};
        
        ReflectionTestUtils.setField(riotRecordService, "riotAPIKey", "test-api-key");

        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(matchIds);

        // 첫 번째 게임: 7킬, 2데스, 5어시스트
        RiotMatchResponse.ParticipantDTO participant1 = createParticipant(gameName, 7, 2, 5, true);
        RiotMatchResponse match1 = createMatchResponse(participant1);
        
        // 두 번째 게임: 4킬, 1데스, 3어시스트
        RiotMatchResponse.ParticipantDTO participant2 = createParticipant(gameName, 4, 1, 3, false);
        RiotMatchResponse match2 = createMatchResponse(participant2);

        when(restTemplate.getForObject(any(String.class), eq(RiotMatchResponse.class)))
                .thenReturn(match1)
                .thenReturn(match2);

        // when
        Recent30GameStatsResponse result = riotRecordService.getRecent30GameStats(gameName, puuid);

        // then
        assertThat(result).isNotNull();
        
        // 총합: 11킬, 3데스, 8어시스트, 2게임
        // 평균: 5.5킬, 1.5데스, 4.0어시스트 (소수점 확인)
        assertThat(result.getRecAvgKills()).isEqualTo(5.5); // 11 / 2 = 5.5
        assertThat(result.getRecAvgDeaths()).isEqualTo(1.5); // 3 / 2 = 1.5
        assertThat(result.getRecAvgAssists()).isEqualTo(4.0); // 8 / 2 = 4.0
    }

    private RiotMatchResponse.ParticipantDTO createParticipant(String gameName, int kills, int deaths, int assists, boolean win) {
        return RiotMatchResponse.ParticipantDTO.builder()
                .riotIdGameName(gameName)
                .championId(1L)
                .win(win)
                .kills(kills)
                .deaths(deaths)
                .assists(assists)
                .totalMinionsKilled(150)
                .neutralMinionsKilled(20)
                .build();
    }

    private RiotMatchResponse createMatchResponse(RiotMatchResponse.ParticipantDTO participant) {
        RiotMatchResponse.InfoDTO info = RiotMatchResponse.InfoDTO.builder()
                .participants(Arrays.asList(participant))
                .gameDuration(1800)
                .queueId(420)
                .build();

        return RiotMatchResponse.builder()
                .info(info)
                .build();
    }
} 