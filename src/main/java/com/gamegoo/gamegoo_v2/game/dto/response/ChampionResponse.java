package com.gamegoo.gamegoo_v2.game.dto.response;

import com.gamegoo.gamegoo_v2.game.domain.Champion;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChampionResponse {

    Long championId;
    String championName;
    Double winRate;
    Integer games;
    Integer wins;
    Double csPerMinute;
    Double kda;
    Integer kills;
    Integer deaths;
    Integer assists;

    public static ChampionResponse of(Champion champion, int wins, int games) {
        double winRate = games > 0 ? (double) wins / games : 0.0;
        return ChampionResponse.builder()
                .championId(champion.getId())
                .championName(champion.getName())
                .winRate(winRate)
                .wins(wins)
                .games(games)
                .csPerMinute(0.0)
                .kda(0.0)
                .kills(0)
                .deaths(0)
                .assists(0)
                .build();
    }

}
