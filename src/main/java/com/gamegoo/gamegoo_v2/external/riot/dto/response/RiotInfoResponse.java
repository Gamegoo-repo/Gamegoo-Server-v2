package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RiotInfoResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String leagueId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String puuid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String queueType;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String tier;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String rank;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int leaguePoints;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int wins;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int losses;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean veteran;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean inactive;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean freshBlood;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean hotStreak;

}
