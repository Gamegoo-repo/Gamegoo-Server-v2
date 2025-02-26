package com.gamegoo.gamegoo_v2.external.riot.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotSummonerResponse {

    String id;
    String accountId;
    String puuid;
    int profileIconId;
    long revisionDate;
    int summonerLevel;

}
