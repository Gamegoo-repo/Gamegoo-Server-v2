package com.gamegoo.gamegoo_v2.external.riot.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "game_match",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_match",
                        columnNames = {"member_id", "match_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_member_game_started",
                        columnList = "member_id, game_started_at DESC"
                )
        }
)
public class GameMatch extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_match_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "match_id", nullable = false, length = 50)
    private String matchId;

    @Column(nullable = false, length = 200)
    private String puuid;

    @Column(name = "game_name", nullable = false, length = 50)
    private String gameName;

    @Column(name = "champion_id", nullable = false)
    private Long championId;

    /**
     * 큐 ID (420: 솔로랭크, 440: 자유랭크, 450: 칼바람)
     */
    @Column(name = "queue_id", nullable = false)
    private Integer queueId;

    @Column(nullable = false)
    private Integer kills;

    @Column(nullable = false)
    private Integer deaths;

    @Column(nullable = false)
    private Integer assists;

    @Column(name = "total_minions_killed", nullable = false)
    private Integer totalMinionsKilled;

    @Column(nullable = false)
    private Boolean win;

    @Column(name = "game_duration", nullable = false)
    private Integer gameDuration;

    @Column(name = "game_started_at", nullable = false)
    private LocalDateTime gameStartedAt;

    @Builder
    public GameMatch(Member member, String matchId, String puuid, String gameName,
                     Long championId, Integer queueId, Integer kills, Integer deaths,
                     Integer assists, Integer totalMinionsKilled, Boolean win,
                     Integer gameDuration, LocalDateTime gameStartedAt) {
        this.member = member;
        this.matchId = matchId;
        this.puuid = puuid;
        this.gameName = gameName;
        this.championId = championId;
        this.queueId = queueId;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.totalMinionsKilled = totalMinionsKilled;
        this.win = win;
        this.gameDuration = gameDuration;
        this.gameStartedAt = gameStartedAt;
    }

    /**
     * ChampionStats 객체로 변환
     * - 기존 통계 계산 로직과 호환성을 위해 ChampionStats 형식으로 변환
     *
     * @return ChampionStats 객체
     */
    public ChampionStats toChampionStats() {
        ChampionStats stats = new ChampionStats(this.championId, this.win);
        stats.setGameTime(this.gameDuration);
        stats.setQueueId(this.queueId);
        stats.setTotalMinionsKilled(this.totalMinionsKilled);
        stats.setKills(this.kills);
        stats.setDeaths(this.deaths);
        stats.setAssists(this.assists);
        return stats;
    }
}