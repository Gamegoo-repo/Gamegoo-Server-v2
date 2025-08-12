package com.gamegoo.gamegoo_v2.account.member.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberChampion extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_champion_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id", nullable = false)
    private Champion champion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 기존 필드들 (프로필용 - 솔로+자유 합친 통계)
    @Column(nullable = false)
    private int wins;

    @Column(nullable = false)
    private int games;

    @Column(nullable = false, columnDefinition = "double default 0.0")
    private double csPerMinute;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int totalCs;

    @Column(nullable = false)
    private int kills;

    @Column(nullable = false)
    private int deaths;

    @Column(nullable = false)
    private int assists;

    // 솔로랭크 전용 통계 (게시판용)
    @Column(nullable = false, columnDefinition = "int default 0")
    private int soloWins;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int soloGames;

    @Column(nullable = false, columnDefinition = "double default 0.0")
    private double soloCsPerMinute;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int soloTotalCs;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int soloKills;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int soloDeaths;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int soloAssists;

    // 자유랭크 전용 통계 (게시판용)
    @Column(nullable = false, columnDefinition = "int default 0")
    private int freeWins;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int freeGames;

    @Column(nullable = false, columnDefinition = "double default 0.0")
    private double freeCsPerMinute;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int freeTotalCs;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int freeKills;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int freeDeaths;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int freeAssists;

    // 칼바람 전용 통계 (게시판용)
    @Column(nullable = false, columnDefinition = "int default 0")
    private int aramWins;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int aramGames;

    @Column(nullable = false, columnDefinition = "double default 0.0")
    private double aramCsPerMinute;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int aramTotalCs;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int aramKills;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int aramDeaths;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int aramAssists;

    public double getKDA() {
        if (deaths == 0) {
            return kills + assists > 0 ? kills + assists : 0;
        }
        return (double) (kills + assists) / deaths;
    }

    public double getSoloKDA() {
        if (soloDeaths == 0) {
            return soloKills + soloAssists > 0 ? soloKills + soloAssists : 0;
        }
        return (double) (soloKills + soloAssists) / soloDeaths;
    }

    public double getFreeKDA() {
        if (freeDeaths == 0) {
            return freeKills + freeAssists > 0 ? freeKills + freeAssists : 0;
        }
        return (double) (freeKills + freeAssists) / freeDeaths;
    }

    public double getAramKDA() {
        if (aramDeaths == 0) {
            return aramKills + aramAssists > 0 ? aramKills + aramAssists : 0;
        }
        return (double) (aramKills + aramAssists) / aramDeaths;
    }

    public static MemberChampion create(Champion champion, Member member, int wins, int games, double csPerMinute, int totalCs, int kills, int deaths, int assists) {
        MemberChampion memberChampion = MemberChampion.builder()
                .champion(champion)
                .wins(wins)
                .games(games)
                .csPerMinute(csPerMinute)
                .totalCs(totalCs)
                .kills(kills)
                .deaths(deaths)
                .assists(assists)
                .build();
        memberChampion.setMember(member);
        return memberChampion;
    }

    @Builder
    private MemberChampion(Champion champion, Member member, int wins, int games, double csPerMinute, int totalCs, int kills, int deaths, int assists) {
        this.champion = champion;
        this.member = member;
        this.wins = wins;
        this.games = games;
        this.csPerMinute = csPerMinute;
        this.totalCs = totalCs;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
    }

    public void setMember(Member member) {
        if (this.member != null) {
            this.member.getMemberChampionList().remove(this);
        }
        this.member = member;
        member.getMemberChampionList().add(this);
    }

    public void updateSoloStats(int wins, int games, double csPerMinute, int totalCs, int kills, int deaths, int assists) {
        this.soloWins = wins;
        this.soloGames = games;
        this.soloCsPerMinute = csPerMinute;
        this.soloTotalCs = totalCs;
        this.soloKills = kills;
        this.soloDeaths = deaths;
        this.soloAssists = assists;
    }

    public void updateFreeStats(int wins, int games, double csPerMinute, int totalCs, int kills, int deaths, int assists) {
        this.freeWins = wins;
        this.freeGames = games;
        this.freeCsPerMinute = csPerMinute;
        this.freeTotalCs = totalCs;
        this.freeKills = kills;
        this.freeDeaths = deaths;
        this.freeAssists = assists;
    }

    public void updateAramStats(int wins, int games, double csPerMinute, int totalCs, int kills, int deaths, int assists) {
        this.aramWins = wins;
        this.aramGames = games;
        this.aramCsPerMinute = csPerMinute;
        this.aramTotalCs = totalCs;
        this.aramKills = kills;
        this.aramDeaths = deaths;
        this.aramAssists = assists;
    }

}
