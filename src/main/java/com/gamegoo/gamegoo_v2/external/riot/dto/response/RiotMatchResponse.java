package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RiotMatchResponse {

    InfoDTO info;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class InfoDTO {

        private List<ParticipantDTO> participants;
        private int gameDuration;

    }

    @Getter
    @Builder
    @AllArgsConstructor
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
