package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.game.domain.Champion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChampionResponse {

    long championId;
    String championName;

    public static ChampionResponse of(Champion champion) {
        return ChampionResponse.builder()
                .championId(champion.getId())
                .championName(champion.getName())
                .build();
    }

}
