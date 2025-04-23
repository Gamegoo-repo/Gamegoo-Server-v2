package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotAccountIdResponse {
    private String id;             // encryptedSummonerId
    private String accountId;      // encryptedAccountId
    private String puuid;          // globally unique player ID
    private int profileIconId;
    private long revisionDate;
    private long summonerLevel;
}
