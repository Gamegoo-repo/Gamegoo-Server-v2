package com.gamegoo.gamegoo_v2.account.member.domain;

import com.gamegoo.gamegoo_v2.external.riot.dto.response.Recent30GameStatsResponse;
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

    // 기존 필드들 (프로필용 - 솔로+자유 합친 통계)
    private int recTotalWins;
    private int recTotalLosses;
    private double recWinRate;
    private double recAvgKDA;
    private double recAvgKills;
    private double recAvgDeaths;
    private double recAvgAssists;
    private double recAvgCsPerMinute;
    private int recTotalCs;

    // 솔로랭크 전용 통계 (게시판용)
    private int soloRecTotalWins;
    private int soloRecTotalLosses;
    private double soloRecWinRate;
    private double soloRecAvgKDA;
    private double soloRecAvgKills;
    private double soloRecAvgDeaths;
    private double soloRecAvgAssists;
    private double soloRecAvgCsPerMinute;
    private int soloRecTotalCs;

    // 자유랭크 전용 통계 (게시판용)
    private int freeRecTotalWins;
    private int freeRecTotalLosses;
    private double freeRecWinRate;
    private double freeRecAvgKDA;
    private double freeRecAvgKills;
    private double freeRecAvgDeaths;
    private double freeRecAvgAssists;
    private double freeRecAvgCsPerMinute;
    private int freeRecTotalCs;

    // 칼바람 전용 통계 (게시판용)
    private int aramRecTotalWins;
    private int aramRecTotalLosses;
    private double aramRecWinRate;
    private double aramRecAvgKDA;
    private double aramRecAvgKills;
    private double aramRecAvgDeaths;
    private double aramRecAvgAssists;
    private double aramRecAvgCsPerMinute;
    private int aramRecTotalCs;

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

    public void updateSoloStats(int wins, int losses, double winRate, double avgKDA, double avgKills,
                               double avgDeaths, double avgAssists, double avgCsPerMinute, int totalCs) {
        this.soloRecTotalWins = wins;
        this.soloRecTotalLosses = losses;
        this.soloRecWinRate = winRate;
        this.soloRecAvgKDA = avgKDA;
        this.soloRecAvgKills = avgKills;
        this.soloRecAvgDeaths = avgDeaths;
        this.soloRecAvgAssists = avgAssists;
        this.soloRecAvgCsPerMinute = avgCsPerMinute;
        this.soloRecTotalCs = totalCs;
    }

    public void updateFreeStats(int wins, int losses, double winRate, double avgKDA, double avgKills,
                               double avgDeaths, double avgAssists, double avgCsPerMinute, int totalCs) {
        this.freeRecTotalWins = wins;
        this.freeRecTotalLosses = losses;
        this.freeRecWinRate = winRate;
        this.freeRecAvgKDA = avgKDA;
        this.freeRecAvgKills = avgKills;
        this.freeRecAvgDeaths = avgDeaths;
        this.freeRecAvgAssists = avgAssists;
        this.freeRecAvgCsPerMinute = avgCsPerMinute;
        this.freeRecTotalCs = totalCs;
    }

    public void updateAramStats(int wins, int losses, double winRate, double avgKDA, double avgKills,
                               double avgDeaths, double avgAssists, double avgCsPerMinute, int totalCs) {
        this.aramRecTotalWins = wins;
        this.aramRecTotalLosses = losses;
        this.aramRecWinRate = winRate;
        this.aramRecAvgKDA = avgKDA;
        this.aramRecAvgKills = avgKills;
        this.aramRecAvgDeaths = avgDeaths;
        this.aramRecAvgAssists = avgAssists;
        this.aramRecAvgCsPerMinute = avgCsPerMinute;
        this.aramRecTotalCs = totalCs;
    }

    public void updateFrom(Recent30GameStatsResponse stats) {
        this.update(
                stats.getRecTotalWins(),
                stats.getRecTotalLosses(),
                stats.getRecWinRate(),
                stats.getRecAvgKDA(),
                stats.getRecAvgKills(),
                stats.getRecAvgDeaths(),
                stats.getRecAvgAssists(),
                stats.getRecAvgCsPerMinute(),
                stats.getRecTotalCs()
        );
    }

    public void updateSoloStatsFrom(Recent30GameStatsResponse stats) {
        this.updateSoloStats(
                stats.getRecTotalWins(),
                stats.getRecTotalLosses(),
                stats.getRecWinRate(),
                stats.getRecAvgKDA(),
                stats.getRecAvgKills(),
                stats.getRecAvgDeaths(),
                stats.getRecAvgAssists(),
                stats.getRecAvgCsPerMinute(),
                stats.getRecTotalCs()
        );
    }

    public void updateFreeStatsFrom(Recent30GameStatsResponse stats) {
        this.updateFreeStats(
                stats.getRecTotalWins(),
                stats.getRecTotalLosses(),
                stats.getRecWinRate(),
                stats.getRecAvgKDA(),
                stats.getRecAvgKills(),
                stats.getRecAvgDeaths(),
                stats.getRecAvgAssists(),
                stats.getRecAvgCsPerMinute(),
                stats.getRecTotalCs()
        );
    }

    public void updateAramStatsFrom(Recent30GameStatsResponse stats) {
        this.updateAramStats(
                stats.getRecTotalWins(),
                stats.getRecTotalLosses(),
                stats.getRecWinRate(),
                stats.getRecAvgKDA(),
                stats.getRecAvgKills(),
                stats.getRecAvgDeaths(),
                stats.getRecAvgAssists(),
                stats.getRecAvgCsPerMinute(),
                stats.getRecTotalCs()
        );
    }

}
