package com.gamegoo.gamegoo_v2.matching.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import com.gamegoo.gamegoo_v2.matching.domain.QMatchingRecord;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.gamegoo.gamegoo_v2.matching.domain.QMatchingRecord.matchingRecord;

@RequiredArgsConstructor
public class MatchingRecordRepositoryCustomImpl implements MatchingRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성 시간 내 만들어진 매칭 기록 조회
     *
     * @param gameMode 게임 모드
     * @param memberId 사용자 id
     * @return 매칭 기록
     */
    @Override
    public List<MatchingRecord> findValidMatchingRecords(LocalDateTime baseTime, GameMode gameMode, Long memberId) {
        QMatchingRecord sub = new QMatchingRecord("sub");

        // baseTime 기준으로 5분 전 계산
        LocalDateTime fiveMinutesAgo = baseTime.minusMinutes(5);

        return queryFactory
                .selectFrom(matchingRecord)
                .where(
                        matchingRecord.createdAt.goe(fiveMinutesAgo),
                        matchingRecord.status.eq(MatchingStatus.PENDING),
                        matchingRecord.gameMode.eq(gameMode),
                        matchingRecord.member.id.ne(memberId),
                        existsValidMatchSubquery(),
                        applyGameModeFilter(gameMode),

                        matchingRecord.createdAt.eq(
                                JPAExpressions
                                        .select(sub.createdAt.max())
                                        .from(sub)
                                        .where(
                                                sub.createdAt.goe(fiveMinutesAgo),
                                                sub.status.eq(MatchingStatus.PENDING),
                                                sub.gameMode.eq(gameMode),
                                                sub.member.id.eq(matchingRecord.member.id)
                                        )
                        )
                )
                .orderBy(matchingRecord.member.id.asc(), matchingRecord.createdAt.desc())
                .fetch();
    }


    /**
     * 해당 회원의 가장 최근 매칭
     *
     * @param member 사용자
     * @return 매칭 기록
     */
    @Override
    public Optional<MatchingRecord> findLatestByMember(Member member) {
        QMatchingRecord matchingRecord = QMatchingRecord.matchingRecord;

        MatchingRecord record = queryFactory
                .selectFrom(matchingRecord)
                .where(member == null ? matchingRecord.member.isNull() : matchingRecord.member.eq(member))
                .orderBy(matchingRecord.createdAt.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(record);
    }

    /**
     * 해당 matchingRecord의 매칭 상대 회원 엔티티 조회
     *
     * @param uuid matchingRecord UUID
     * @return 매칭 상대 회원, 매칭 상대가 존재하지 않으면 Optional empty를 리턴
     */
    @Override
    public Optional<Member> findTargetMemberByUuid(String uuid) {
        QMatchingRecord mr1 = QMatchingRecord.matchingRecord;
        QMatchingRecord mr2 = new QMatchingRecord("mr2");

        Member targetMember = queryFactory
                .select(mr2.member)
                .from(mr1)
                .join(mr1.targetMatchingRecord, mr2)
                .where(mr1.matchingUuid.eq(uuid))
                .fetchOne();

        return Optional.ofNullable(targetMember);
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
        return validateSoloRankRange(matchingRecord.tier);
    }

    /**
     * FREE 모드 - 자유 랭크 제한 검증
     */
    private BooleanExpression validateFreeRankFilter() {
        return matchingRecord.tier.in(Tier.UNRANKED, Tier.IRON, Tier.BRONZE, Tier.SILVER, Tier.GOLD)
                .and(matchingRecord.tier.notIn(Tier.EMERALD, Tier.DIAMOND, Tier.MASTER, Tier.GRANDMASTER,
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
     * 매칭 가능한 포지션 조건
     */
    private BooleanExpression isValidMatchingPosition(QMatchingRecord myRecord, QMatchingRecord otherRecord) {
        // ANY가 하나라도 포함되어 있으면 무조건 매칭 허용
        BooleanExpression hasAny = myRecord.mainP.eq(Position.ANY)
                .or(myRecord.subP.eq(Position.ANY))
                .or(otherRecord.mainP.eq(Position.ANY))
                .or(otherRecord.subP.eq(Position.ANY));

        // mainP와 subP가 모두 겹치는 경우 (매칭 불가 조건)
        BooleanExpression exactConflict = myRecord.mainP.eq(otherRecord.mainP)
                .and(myRecord.subP.eq(otherRecord.subP));

        // 매칭은 ANY가 포함되거나, 포지션이 완전히 같지 않은 경우만 허용
        return hasAny.or(exactConflict.not());
    }

    /**
     * 개인 랭크 제한 검증 (SOLO 모드 전용)
     */
    private BooleanExpression validateSoloRankRange(EnumPath<Tier> tierPath) {

        // UNRANKED ↔ UNRANKED 전용 매칭
        BooleanExpression unrankedOnly =
                tierPath.eq(Tier.UNRANKED)
                        .and(matchingRecord.tier.eq(Tier.UNRANKED));

        // 기존 랭크 매칭 규칙 (UNRANKED 제외)
        BooleanExpression rankedRange =
                tierPath.eq(Tier.IRON)
                        .or(tierPath.eq(Tier.BRONZE))
                        .and(matchingRecord.tier.in(Tier.IRON, Tier.BRONZE, Tier.SILVER))
                        .or(tierPath.eq(Tier.SILVER)
                                .and(matchingRecord.tier.in(Tier.IRON, Tier.BRONZE, Tier.SILVER, Tier.GOLD)))
                        .or(tierPath.eq(Tier.GOLD)
                                .and(matchingRecord.tier.in(Tier.SILVER, Tier.GOLD, Tier.PLATINUM)))
                        .or(tierPath.eq(Tier.PLATINUM)
                                .and(matchingRecord.tier.in(Tier.GOLD, Tier.PLATINUM, Tier.EMERALD)))
                        .or(tierPath.eq(Tier.EMERALD)
                                .and(matchingRecord.tier.in(Tier.PLATINUM, Tier.EMERALD, Tier.DIAMOND)))
                        .or(tierPath.eq(Tier.DIAMOND)
                                .and(matchingRecord.tier.in(Tier.EMERALD, Tier.DIAMOND)))
                        .or(tierPath.eq(Tier.MASTER)
                                .and(matchingRecord.tier.in(Tier.MASTER, Tier.GRANDMASTER)))
                        .or(tierPath.eq(Tier.GRANDMASTER)
                                .and(matchingRecord.tier.in(Tier.MASTER, Tier.GRANDMASTER)));

        // UNRANKED 전용 OR 기존 랭크 규칙
        return unrankedOnly.or(rankedRange);
    }

}
