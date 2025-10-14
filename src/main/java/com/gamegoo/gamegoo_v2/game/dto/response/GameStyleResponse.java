package com.gamegoo.gamegoo_v2.game.dto.response;

import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameStyleResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long gameStyleId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String gameStyleName;

    public static GameStyleResponse of(GameStyle gameStyle) {
        return GameStyleResponse.builder()
                .gameStyleId(gameStyle.getId())
                .gameStyleName(gameStyle.getStyleName())
                .build();
    }

}
