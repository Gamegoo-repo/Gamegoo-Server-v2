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

    public double getKDA() {
        if (deaths == 0) {
            return kills + assists > 0 ? kills + assists : 0;
        }
        return (double) (kills + assists) / deaths;
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

}
