package com.gamegoo.gamegoo_v2.account.member.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberRecentStats {
    @Id
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;

    private int recTotalWins;
    private int recTotalLosses;
    private double recWinRate;
    private double recAvgKDA;
    private double recAvgKills;
    private double recAvgDeaths;
    private double recAvgAssists;
    private double recAvgCsPerMinute;
    private int recTotalCs;

    public void update(int recTotalWins, int recTotalLosses, double recWinRate, double recAvgKDA, double recAvgKills, double recAvgDeaths, double recAvgAssists, double recAvgCsPerMinute, int recTotalCs) {
        this.recTotalWins = recTotalWins;
        this.recTotalLosses = recTotalLosses;
        this.recWinRate = recWinRate;
        this.recAvgKDA = recAvgKDA;
        this.recAvgKills = recAvgKills;
        this.recAvgDeaths = recAvgDeaths;
        this.recAvgAssists = recAvgAssists;
        this.recAvgCsPerMinute = recAvgCsPerMinute;
        this.recTotalCs = recTotalCs;
    }
} 