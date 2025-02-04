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
    @Column(nullable = false)
    private GameMode gameMode;

    @Enumerated(EnumType.STRING)
    @Column
    private Position mainPosition;

    @Enumerated(EnumType.STRING)
    @Column
    private Position subPosition;

    @Enumerated(EnumType.STRING)
    @Column
    private Position wantPosition;

    @Enumerated(EnumType.STRING)
    @Column
    private Mike mike;

    @Enumerated(EnumType.STRING)
    @Column
    private Tier soloTier;

    @Column
    private int soloRank;

    @Column(nullable = false)
    private double soloWinRate;

    @Enumerated(EnumType.STRING)
    @Column
    private Tier freeTier;

    @Column
    private int freeRank;

    @Column
    private double freeWinRate;

    @Enumerated(EnumType.STRING)
    @Column
    private MatchingType matchingType;

    @Column
    private int mannerLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingStatus status = MatchingStatus.PENDING;

    @Column
    private Boolean mannerMessageSent = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    private Member targetMember;

    // MatchingRecord 생성 메서드
    public static MatchingRecord create(GameMode gameMode, MatchingType matchingType, Member member) {
        return MatchingRecord.builder()
                .gameMode(gameMode)
                .mainPosition(member.getMainPosition())
                .subPosition(member.getSubPosition())
                .wantPosition(member.getWantPosition())
                .mike(member.getMike())
                .soloTier(member.getTier()) // TODO:
                .soloRank(member.getGameRank()) // TODO:
                .soloWinRate(member.getWinRate()) // TODO:
                .freeTier(member.getTier()) // TODO:
                .freeRank(member.getGameRank()) // TODO:
                .freeWinRate(member.getWinRate()) // TODO:
                .matchingType(matchingType)
                .mannerLevel(member.getMannerLevel())
                .member(member)
                .build();
    }

    // MatchingRecord Builder
    @Builder
    private MatchingRecord(GameMode gameMode, Position mainPosition, Position subPosition, Position wantPosition,
                           Mike mike, Tier soloTier, int soloRank, double soloWinRate, Tier freeTier,
                           int freeRank, double freeWinRate, MatchingType matchingType, int mannerLevel,
                           Member member) {
        this.gameMode = gameMode;
        this.mainPosition = mainPosition;
        this.subPosition = subPosition;
        this.wantPosition = wantPosition;
        this.mike = mike;
        this.soloTier = soloTier;
        this.soloRank = soloRank;
        this.soloWinRate = soloWinRate;
        this.freeTier = freeTier;
        this.freeRank = freeRank;
        this.freeWinRate = freeWinRate;
        this.matchingType = matchingType;
        this.mannerLevel = mannerLevel;
        this.member = member;
        this.targetMember = null;
    }

    // status 변경
    public void updateStatus(MatchingStatus status) {
        this.status = status;
    }

    // targetMember 설정
    public void updateTargetMember(Member member) {
        this.targetMember = member;
    }

    public void updateMannerMessageSent(Boolean mannerMessageSent) {
        this.mannerMessageSent = mannerMessageSent;
    }

}
