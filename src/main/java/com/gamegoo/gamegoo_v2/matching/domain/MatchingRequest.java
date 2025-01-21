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
public class MatchingRequest extends BaseDateTimeEntity {

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

    @Column(nullable = false)
    private double freeWinRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // MatchingRequest 생성 메서드
    public static MatchingRequest create(GameMode gameMode, Position mainPosition, Position subPosition,
                                         Position wantPosition, Mike mike, Tier soloTier, int soloRank,
                                         double soloWinRate, Tier freeTier, int freeRank, double freeWinRate,
                                         Member member) {
        return MatchingRequest.builder()
                .gameMode(gameMode)
                .mainPosition(mainPosition)
                .subPosition(subPosition)
                .wantPosition(wantPosition)
                .mike(mike)
                .soloTier(soloTier)
                .soloRank(soloRank)
                .soloWinRate(soloWinRate)
                .freeTier(freeTier)
                .freeRank(freeRank)
                .freeWinRate(freeWinRate)
                .member(member)
                .build();
    }

    // MatchingRequest Builder
    @Builder
    private MatchingRequest(GameMode gameMode, Position mainPosition, Position subPosition, Position wantPosition,
                            Mike mike, Tier soloTier, int soloRank, double soloWinRate, Tier freeTier,
                            int freeRank, double freeWinRate, Member member) {
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
        this.member = member;
    }

}
