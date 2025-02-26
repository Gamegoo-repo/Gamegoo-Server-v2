package com.gamegoo.gamegoo_v2.game.dto.response;

import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameStyleResponse {

    Long gameStyleId;
    String gameStyleName;

    public static GameStyleResponse of(GameStyle gameStyle) {
        return GameStyleResponse.builder()
                .gameStyleId(gameStyle.getId())
                .gameStyleName(gameStyle.getStyleName())
                .build();
    }

}
