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

/**
 * Riot API로부터 가져온 개별 매치 데이터를 저장하는 엔티티
 *
 * <p>주요 목적:</p>
 * <ul>
 *   <li>Riot API 호출 횟수 최소화 (증분 업데이트)</li>
 *   <li>이미 저장된 매치는 재조회하지 않음</li>
 *   <li>최근 30개 매치 기반 통계 계산</li>
 * </ul>
 *
 * <p>제약 조건:</p>
 * <ul>
 *   <li>유니크 제약: (member_id, match_id) - 중복 저장 방지</li>
 *   <li>인덱스: (member_id, game_started_at DESC) - 최근 매치 빠른 조회</li>
 * </ul>
 */
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

    /**
     * 매치를 플레이한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * Riot API의 매치 고유 ID (예: "KR_1234567890")
     */
    @Column(name = "match_id", nullable = false, length = 50)
    private String matchId;

    /**
     * 사용자의 Riot PUUID
     */
    @Column(nullable = false, length = 200)
    private String puuid;

    /**
     * 사용자의 게임 닉네임 (Riot ID Game Name)
     */
    @Column(name = "game_name", nullable = false, length = 50)
    private String gameName;

    /**
     * 플레이한 챔피언 ID
     */
    @Column(name = "champion_id", nullable = false)
    private Long championId;

    /**
     * 큐 ID (420: 솔로랭크, 440: 자유랭크, 450: 칼바람)
     */
    @Column(name = "queue_id", nullable = false)
    private Integer queueId;

    /**
     * 킬 수
     */
    @Column(nullable = false)
    private Integer kills;

    /**
     * 데스 수
     */
    @Column(nullable = false)
    private Integer deaths;

    /**
     * 어시스트 수
     */
    @Column(nullable = false)
    private Integer assists;

    /**
     * 총 CS (미니언 + 정글 몹)
     */
    @Column(name = "total_minions_killed", nullable = false)
    private Integer totalMinionsKilled;

    /**
     * 승리 여부
     */
    @Column(nullable = false)
    private Boolean win;

    /**
     * 게임 시간 (초 단위)
     */
    @Column(name = "game_duration", nullable = false)
    private Integer gameDuration;

    /**
     * 게임이 시작된 시각 (Riot API 기준)
     * - 최근 30개 매치를 조회할 때 이 필드로 정렬
     */
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