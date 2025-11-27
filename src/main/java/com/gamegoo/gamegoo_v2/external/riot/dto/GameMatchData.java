package com.gamegoo.gamegoo_v2.external.riot.dto;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.external.riot.domain.GameMatch;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotMatchResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class GameMatchData {
    private Long championId;
    private Integer queueId;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer totalMinionsKilled;
    private Boolean win;
    private Integer gameDuration;
    private LocalDateTime gameStartedAt;

    public static GameMatchData from(RiotMatchResponse.ParticipantDTO participant,
                                      int queueId, int gameDuration,
                                      LocalDateTime gameStartedAt) {
        int totalCs = Math.max(0, participant.getTotalMinionsKilled()
                + participant.getNeutralMinionsKilled());

        return GameMatchData.builder()
                .championId(participant.getChampionId())
                .queueId(queueId)
                .kills(participant.getKills())
                .deaths(participant.getDeaths())
                .assists(participant.getAssists())
                .totalMinionsKilled(totalCs)
                .win(participant.isWin())
                .gameDuration(gameDuration)
                .gameStartedAt(gameStartedAt)
                .build();
    }

    public GameMatch toEntity(Member member, String matchId, String puuid, String gameName) {
        return GameMatch.builder()
                .member(member)
                .matchId(matchId)
                .puuid(puuid)
                .gameName(gameName)
                .championId(this.championId)
                .queueId(this.queueId)
                .kills(this.kills)
                .deaths(this.deaths)
                .assists(this.assists)
                .totalMinionsKilled(this.totalMinionsKilled)
                .win(this.win)
                .gameDuration(this.gameDuration)
                .gameStartedAt(this.gameStartedAt)
                .build();
    }
}
