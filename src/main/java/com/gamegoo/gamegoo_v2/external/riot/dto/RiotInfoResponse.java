package com.gamegoo.gamegoo_v2.external.riot.dto;

import lombok.Getter;

@Getter
public class RiotInfoResponse {

    String leagueId;
    String queueType;
    String tier;
    String rank;
    String summonerId;
    int leaguePoints;
    int wins;
    int losses;
    boolean veteran;
    boolean inactive;
    boolean freshBlood;
    boolean hotStreak;

}
