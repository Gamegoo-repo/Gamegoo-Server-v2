package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RiotMatchResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    InfoDTO info;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class InfoDTO {

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private List<ParticipantDTO> participants;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private int gameDuration;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private int queueId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private long gameCreation; 

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ParticipantDTO {

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String puuid;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String riotIdGameName;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String gameMode;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Long championId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private boolean win;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private int totalMinionsKilled;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private int neutralMinionsKilled;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private int kills;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private int deaths;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private int assists;

    }

}
