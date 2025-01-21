package com.gamegoo.gamegoo_v2.matching.domain;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
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


}
