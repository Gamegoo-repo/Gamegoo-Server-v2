package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class RiotMatchResponse {

    InfoDTO info;

    @Getter
    public static class InfoDTO {

        private List<ParticipantDTO> participants;
        private int gameDuration;

    }

    @Getter
    public static class ParticipantDTO {

        private String riotIdGameName;
        private String gameMode;
        private Long championId;
        private boolean win;
        private int totalMinionsKilled;
        private int kills;
        private int deaths;
        private int assists;

    }

}
