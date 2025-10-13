package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotSummonerResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String accountId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String puuid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int profileIconId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long revisionDate;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int summonerLevel;

}
