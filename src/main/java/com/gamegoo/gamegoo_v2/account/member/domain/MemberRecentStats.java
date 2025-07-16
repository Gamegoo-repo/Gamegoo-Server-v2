package com.gamegoo.gamegoo_v2.account.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void setMember(Member member) {
        this.member = member;
        if (member.getMemberRecentStats() != this) {
            member.setMemberRecentStats(this);
        }
    }

    public void update(int recTotalWins, int recTotalLosses, double recWinRate, double recAvgKDA, double recAvgKills,
                       double recAvgDeaths, double recAvgAssists, double recAvgCsPerMinute, int recTotalCs) {
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
