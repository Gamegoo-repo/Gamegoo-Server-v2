package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import lombok.Getter;

@Getter
public class RiotInfoResponse {

    String leagueId;
    String puuid;
    String queueType;
    String tier;
    String rank;
    int leaguePoints;
    int wins;
    int losses;
    boolean veteran;
    boolean inactive;
    boolean freshBlood;
    boolean hotStreak;

}
