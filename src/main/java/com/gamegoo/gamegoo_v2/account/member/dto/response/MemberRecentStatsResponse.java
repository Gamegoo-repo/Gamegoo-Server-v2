package com.gamegoo.gamegoo_v2.account.member.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberRecentStatsResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int recTotalWins;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int recTotalLosses;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double recWinRate;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double recAvgKDA;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double recAvgKills;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double recAvgDeaths;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double recAvgAssists;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double recAvgCsPerMinute;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int recTotalCs;

    public static MemberRecentStatsResponse from(MemberRecentStats memberRecentStats) {
        if (memberRecentStats == null) {
            return null;
        }

        try {
            return new MemberRecentStatsResponse(
                    memberRecentStats.getRecTotalWins(),
                    memberRecentStats.getRecTotalLosses(),
                    memberRecentStats.getRecWinRate(),
                    memberRecentStats.getRecAvgKDA(),
                    memberRecentStats.getRecAvgKills(),
                    memberRecentStats.getRecAvgDeaths(),
                    memberRecentStats.getRecAvgAssists(),
                    memberRecentStats.getRecAvgCsPerMinute(),
                    memberRecentStats.getRecTotalCs()
            );
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return null;
        }
    }

    public static MemberRecentStatsResponse fromGameMode(MemberRecentStats memberRecentStats, GameMode gameMode) {
        if (memberRecentStats == null) {
            return null;
        }
        return switch (gameMode) {
            case SOLO, FAST -> new MemberRecentStatsResponse(
                    memberRecentStats.getSoloRecTotalWins(),
                    memberRecentStats.getSoloRecTotalLosses(),
                    memberRecentStats.getSoloRecWinRate(),
                    memberRecentStats.getSoloRecAvgKDA(),
                    memberRecentStats.getSoloRecAvgKills(),
                    memberRecentStats.getSoloRecAvgDeaths(),
                    memberRecentStats.getSoloRecAvgAssists(),
                    memberRecentStats.getSoloRecAvgCsPerMinute(),
                    memberRecentStats.getSoloRecTotalCs()
            );
            case FREE -> new MemberRecentStatsResponse(
                    memberRecentStats.getFreeRecTotalWins(),
                    memberRecentStats.getFreeRecTotalLosses(),
                    memberRecentStats.getFreeRecWinRate(),
                    memberRecentStats.getFreeRecAvgKDA(),
                    memberRecentStats.getFreeRecAvgKills(),
                    memberRecentStats.getFreeRecAvgDeaths(),
                    memberRecentStats.getFreeRecAvgAssists(),
                    memberRecentStats.getFreeRecAvgCsPerMinute(),
                    memberRecentStats.getFreeRecTotalCs()
            );
            case ARAM -> new MemberRecentStatsResponse(
                    memberRecentStats.getAramRecTotalWins(),
                    memberRecentStats.getAramRecTotalLosses(),
                    memberRecentStats.getAramRecWinRate(),
                    memberRecentStats.getAramRecAvgKDA(),
                    memberRecentStats.getAramRecAvgKills(),
                    memberRecentStats.getAramRecAvgDeaths(),
                    memberRecentStats.getAramRecAvgAssists(),
                    memberRecentStats.getAramRecAvgCsPerMinute(),
                    memberRecentStats.getAramRecTotalCs()
            );
        };
    }
}
