package com.gamegoo.gamegoo_v2.matching.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private Position wantP;

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

    @Column
    private Boolean mannerMessageSent = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_matchingUuid")
    private MatchingRecord targetMatchingRecord;

    // MatchingRecord 생성 메서드
    public static MatchingRecord create(GameMode gameMode, MatchingType matchingType, Member member) {
        return MatchingRecord.builder()
                .gameMode(gameMode)
                .mainP(member.getMainP())
                .subP(member.getSubP())
                .wantP(member.getWantP())
                .mike(member.getMike())
                .tier(member.getSoloTier()) // TODO:
                .gameRank(member.getSoloRank()) // TODO:
                .winrate(member.getSoloWinRate()) // TODO:
                .matchingType(matchingType)
                .mannerLevel(member.getMannerLevel())
                .member(member)
                .build();
    }

    // MatchingRecord Builder
    @Builder
    private MatchingRecord(GameMode gameMode, Position mainP, Position subP, Position wantP,
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

    // targetMember 설정
    public void updateTargetMatchingRecord(MatchingRecord targetMatchingRecord) {
        this.targetMatchingRecord = targetMatchingRecord;
    }

    public void updateMannerMessageSent(Boolean mannerMessageSent) {
        this.mannerMessageSent = mannerMessageSent;
    }

}
