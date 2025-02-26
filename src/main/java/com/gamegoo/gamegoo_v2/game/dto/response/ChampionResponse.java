package com.gamegoo.gamegoo_v2.game.dto.response;

import com.gamegoo.gamegoo_v2.game.domain.Champion;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChampionResponse {

    Long championId;
    String championName;

    public static ChampionResponse of(Champion champion) {
        return ChampionResponse.builder()
                .championId(champion.getId())
                .championName(champion.getName())
                .build();
    }

}
