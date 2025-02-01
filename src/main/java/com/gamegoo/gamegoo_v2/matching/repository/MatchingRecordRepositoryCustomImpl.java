package com.gamegoo.gamegoo_v2.matching.repository;

import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.matching.domain.QMatchingRecord;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.gamegoo.gamegoo_v2.matching.domain.QMatchingRecord.matchingRecord;

@RequiredArgsConstructor
public class MatchingRecordRepositoryCustomImpl implements MatchingRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MatchingRecord> findValidMatchingRecords(LocalDateTime createdAt, GameMode gameMode) {
        return queryFactory.selectFrom(matchingRecord)
                .where(
                        matchingRecord.createdAt.gt(createdAt),    // 5분 이내
                        matchingRecord.status.eq(MatchingStatus.PENDING),  // 상태 PENDING
                        matchingRecord.gameMode.eq(gameMode),   // 특정 게임 모드
                        existsValidMatchSubquery(),  // 유효한 매칭만 가져오기
                        applyGameModeFilter(gameMode) // 게임모드별 추가 필터 적용 (SOLO, FREE)
                )
                .orderBy(matchingRecord.member.id.asc(), matchingRecord.createdAt.desc()) // 최신순 정렬
                .fetch();
    }

    /**
     * 게임 모드에 따른 추가 필터 적용
     */
    private BooleanExpression applyGameModeFilter(GameMode gameMode) {
        return switch (gameMode) {
            case SOLO -> validateSoloRankFilter(); // 개인 랭크 티어 검증
            case FREE -> validateFreeRankFilter(); // 자유 랭크 티어 검증
            default -> Expressions.TRUE; // 다른 게임 모드는 필터 없음
        };
    }

    /**
     * SOLO 모드 - 개인 랭크 제한 검증 (validateSoloRankRange 적용)
     */
    private BooleanExpression validateSoloRankFilter() {
        return validateSoloRankRange(matchingRecord.soloTier);
    }

    /**
     * FREE 모드 - 자유 랭크 제한 검증
     */
    private BooleanExpression validateFreeRankFilter() {
        return matchingRecord.freeTier.in(Tier.IRON, Tier.BRONZE, Tier.SILVER, Tier.GOLD)
                .and(matchingRecord.freeTier.notIn(Tier.EMERALD, Tier.DIAMOND, Tier.MASTER, Tier.GRANDMASTER,
                        Tier.CHALLENGER));
    }


    /**
     * 매칭 유효성 검사 서브쿼리
     */
    private BooleanExpression existsValidMatchSubquery() {
        QMatchingRecord otherRecord = new QMatchingRecord("otherRecord");

        return JPAExpressions.selectOne()
                .from(otherRecord)
                .where(
                        otherRecord.member.id.ne(matchingRecord.member.id), // 다른 멤버와의 매칭
                        isValidMatchingPosition(matchingRecord, otherRecord) // 포지션 조건 체크
                )
                .exists();
    }

    /**
     * 매칭 가능한 포지션 조건 ]
     */
    private BooleanExpression isValidMatchingPosition(QMatchingRecord myRecord, QMatchingRecord otherRecord) {
        BooleanExpression condition1 = myRecord.mainPosition.ne(Position.ANY)
                .and(otherRecord.subPosition.ne(Position.ANY))
                .and(myRecord.mainPosition.eq(otherRecord.subPosition)
                        .or(otherRecord.mainPosition.eq(myRecord.subPosition)))
                .and(myRecord.wantPosition.eq(otherRecord.subPosition));

        BooleanExpression condition2 = myRecord.subPosition.ne(Position.ANY)
                .and(otherRecord.wantPosition.ne(Position.ANY))
                .and(otherRecord.wantPosition.eq(myRecord.subPosition))
                .and(otherRecord.mainPosition.eq(myRecord.subPosition)
                        .or(myRecord.mainPosition.eq(otherRecord.subPosition)));

        BooleanExpression condition3 = myRecord.mainPosition.ne(Position.ANY)
                .and(myRecord.subPosition.ne(Position.ANY))
                .and(otherRecord.mainPosition.ne(Position.ANY))
                .and(otherRecord.subPosition.ne(Position.ANY))
                .and(myRecord.mainPosition.eq(otherRecord.mainPosition)
                        .or(myRecord.mainPosition.eq(otherRecord.subPosition)))
                .and(myRecord.subPosition.eq(otherRecord.mainPosition)
                        .or(myRecord.subPosition.eq(otherRecord.subPosition)))
                .not();

        return condition1.or(condition2).or(condition3);
    }


    /**
     * 개인 랭크 제한 검증 (SOLO 모드 전용) - QueryDSL에서 적용 가능하도록 수정
     */
    private BooleanExpression validateSoloRankRange(EnumPath<Tier> tierPath) {
        return tierPath.eq(Tier.IRON).or(tierPath.eq(Tier.BRONZE)).and(matchingRecord.soloTier.in(Tier.IRON,
                        Tier.BRONZE, Tier.SILVER))
                .or(tierPath.eq(Tier.SILVER).and(matchingRecord.soloTier.in(Tier.IRON, Tier.BRONZE, Tier.SILVER,
                        Tier.GOLD)))
                .or(tierPath.eq(Tier.GOLD).and(matchingRecord.soloTier.in(Tier.SILVER, Tier.GOLD, Tier.PLATINUM)))
                .or(tierPath.eq(Tier.PLATINUM).and(matchingRecord.soloTier.in(Tier.GOLD, Tier.PLATINUM, Tier.EMERALD)))
                .or(tierPath.eq(Tier.EMERALD).and(matchingRecord.soloTier.in(Tier.PLATINUM, Tier.EMERALD,
                        Tier.DIAMOND)))
                .or(tierPath.eq(Tier.DIAMOND).and(matchingRecord.soloTier.in(Tier.EMERALD, Tier.DIAMOND)))
                .or(Expressions.FALSE); // 마스터 이상 필터 적용 안 함
    }


}
