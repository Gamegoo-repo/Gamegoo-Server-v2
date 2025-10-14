package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotAccountIdResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;             // encryptedSummonerId
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountId;      // encryptedAccountId
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String puuid;          // globally unique player ID
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int profileIconId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private long revisionDate;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private long summonerLevel;
}
