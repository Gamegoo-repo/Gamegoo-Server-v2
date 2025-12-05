package com.gamegoo.gamegoo_v2.matching.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingRecord extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "matching_uuid")
    private String matchingUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private GameMode gameMode;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private Position mainP;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private Position subP;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "matching_record_want_positions", joinColumns = @JoinColumn(name = "matching_uuid"))
    @Column(name = "want_position", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<Position> wantP = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private Mike mike;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private Tier tier;

    @Column
    private int gameRank;

    @Column(nullable = false)
    private double winrate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private MatchingType matchingType;

    @Column
    private int mannerLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private MatchingStatus status = MatchingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private MannerMessageStatus mannerMessageSent = MannerMessageStatus.NOT_REQUIRED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_matching_uuid")
    private MatchingRecord targetMatchingRecord;

    public static MatchingRecord create(GameMode gameMode, MatchingType matchingType, Member member) {
        return MatchingRecord.builder()
                .gameMode(gameMode)
                .mainP(member.getMainP())
                .subP(member.getSubP())
                .wantP(new ArrayList<>(member.getWantP()))
                .mike(member.getMike())
                .tier(getTierByGameMode(gameMode, member))
                .gameRank(getGameRankByGameMode(gameMode, member))
                .winrate(getWinRateByGameMode(gameMode, member))
                .matchingType(matchingType)
                .mannerLevel(member.getMannerLevel())
                .member(member)
                .build();
    }

    private static Tier getTierByGameMode(GameMode gameMode, Member member) {
        if (gameMode == GameMode.FREE) {
            return member.getFreeTier();
        }
        return member.getSoloTier();
    }

    private static int getGameRankByGameMode(GameMode gameMode, Member member) {
        if (gameMode == GameMode.FREE) {
            return member.getFreeRank();
        }
        return member.getSoloRank();
    }

    private static double getWinRateByGameMode(GameMode gameMode, Member member) {
        return switch (gameMode) {
            case FREE -> member.getFreeWinRate();
            case ARAM -> member.getAramWinRate();
            default -> member.getSoloWinRate(); // SOLO, FAST
        };
    }

    // MatchingRecord Builder
    @Builder
    private MatchingRecord(GameMode gameMode, Position mainP, Position subP, List<Position> wantP,
                           Mike mike, Tier tier, int gameRank, double winrate, MatchingType matchingType,
                           int mannerLevel, Member member) {
        this.gameMode = gameMode;
        this.mainP = mainP;
        this.subP = subP;
        this.wantP = wantP;
        this.mike = mike;
        this.tier = tier;
        this.gameRank = gameRank;
        this.winrate = winrate;
        this.matchingType = matchingType;
        this.mannerLevel = mannerLevel;
        this.member = member;
        this.targetMatchingRecord = null;
    }

    // status 변경
    public void updateStatus(MatchingStatus status) {
        this.status = status;
    }

    // 테스트용 createAt 수정
    public void updateCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // targetMember 설정
    public void updateTargetMatchingRecord(MatchingRecord targetMatchingRecord) {
        this.targetMatchingRecord = targetMatchingRecord;
    }

    public void updateMannerMessageSent(MannerMessageStatus mannerMessageSent) {
        this.mannerMessageSent = mannerMessageSent;
    }

}
