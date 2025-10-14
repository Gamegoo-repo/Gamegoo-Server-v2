package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotPuuidGameNameResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String puuid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String gameName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String tagLine;

}
