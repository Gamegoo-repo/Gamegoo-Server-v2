package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotMatchResponse;
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
                .build();

        RiotMatchResponse.InfoDTO info = RiotMatchResponse.InfoDTO.builder()
                .participants(Arrays.asList(participant))
                .gameDuration(1800) // 30분
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
                .build();

        RiotMatchResponse.InfoDTO info = RiotMatchResponse.InfoDTO.builder()
                .participants(Arrays.asList(participant))
                .gameDuration(1800)
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
                .build();

        RiotMatchResponse.InfoDTO info1 = RiotMatchResponse.InfoDTO.builder()
                .participants(Arrays.asList(participant1))
                .gameDuration(1800)
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
                .build();

        RiotMatchResponse.InfoDTO info2 = RiotMatchResponse.InfoDTO.builder()
                .participants(Arrays.asList(participant2))
                .gameDuration(1800)
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
} 