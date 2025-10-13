package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.game.domain.Champion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChampionResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long championId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String championName;

    public static ChampionResponse of(Champion champion) {
        return ChampionResponse.builder()
                .championId(champion.getId())
                .championName(champion.getName())
                .build();
    }

}
