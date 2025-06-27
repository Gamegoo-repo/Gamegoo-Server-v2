package com.gamegoo.gamegoo_v2.account.member.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberRecentStatsResponse {
    private int recTotalWins;
    private int recTotalLosses;
    private double recWinRate;
    private double recAvgKDA;
    private double recAvgCsPerMinute;
    private int recTotalCs;

    public static MemberRecentStatsResponse from(MemberRecentStats memberRecentStats) {
        if (memberRecentStats == null) {
            return null;
        }
        
        return new MemberRecentStatsResponse(
                memberRecentStats.getRecTotalWins(),
                memberRecentStats.getRecTotalLosses(),
                memberRecentStats.getRecWinRate(),
                memberRecentStats.getRecAvgKDA(),
                memberRecentStats.getRecAvgCsPerMinute(),
                memberRecentStats.getRecTotalCs()
        );
    }
}