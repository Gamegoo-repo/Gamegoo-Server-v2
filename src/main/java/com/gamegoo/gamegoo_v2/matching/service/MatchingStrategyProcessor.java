package com.gamegoo.gamegoo_v2.matching.service;

import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingStrategyProcessor {

    private final MatchingScoreCalculator matchingScoreCalculator;

    /**
     * 정밀 매칭 우선순위 계산
     */
    public int calculatePrecisePriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        return matchingScoreCalculator.getMannerPriority(
                otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 16, 4);
    }

    /**
     * 빠른대전 우선순위 계산
     */
    public int calculateFastPriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        int priority = 0;

        // 매너 우선순위
        priority += matchingScoreCalculator.getMannerPriority(
                otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 16, 4);

        // TODO: 티어 및 랭킹 점수 계산
        // priority += matchingPriorityEvaluateService.getTierRankPriority(myRecord.getTier(), myRecord.getRank(),
        // otherRecord.getTier(), otherRecord.getRank(), 40, 4);

        // 포지션 우선순위
        priority += matchingScoreCalculator.getPositionPriority(
                myRecord.getWantPosition(), otherRecord.getMainPosition(), otherRecord.getSubPosition(), 3, 2, 1);
        priority += matchingScoreCalculator.getPositionPriority(
                otherRecord.getWantPosition(), myRecord.getMainPosition(), myRecord.getSubPosition(), 3, 2, 1);

        // 마이크 우선순위
        priority += matchingScoreCalculator.getMikePriority(myRecord.getMike(), otherRecord.getMike(), 3);

        return priority;
    }

    /**
     * 개인 랭크 모드 우선순위 계산
     */
    public int calculateSoloPriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        int priority = 0;

        // 매너 우선순위
        priority += matchingScoreCalculator.getMannerPriority(
                otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 16, 4);

        // 티어 및 랭킹 점수 계산
        priority += matchingScoreCalculator.getTierRankPriority(
                myRecord.getSoloTier(), myRecord.getSoloRank(),
                otherRecord.getSoloTier(), otherRecord.getSoloRank(), 40, 4);

        // 포지션 우선순위
        priority += matchingScoreCalculator.getPositionPriority(
                myRecord.getWantPosition(), otherRecord.getMainPosition(), otherRecord.getSubPosition(), 3, 2, 1);
        priority += matchingScoreCalculator.getPositionPriority(
                otherRecord.getWantPosition(), myRecord.getMainPosition(), myRecord.getSubPosition(), 3, 2, 1);

        // 마이크 우선순위
        priority += matchingScoreCalculator.getMikePriority(myRecord.getMike(), otherRecord.getMike(), 5);

        return priority;
    }

    /**
     * FREE 모드 우선순위 계산
     */
    public int calculateFreePriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        int priority = 0;

        // 매너 우선순위
        priority += matchingScoreCalculator.getMannerPriority(
                otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 16, 4);

        // 티어 및 랭킹 점수 계산
        priority += matchingScoreCalculator.getTierRankPriority(
                myRecord.getFreeTier(), myRecord.getFreeRank(),
                otherRecord.getFreeTier(), otherRecord.getFreeRank(), 40, 4);

        // 포지션 우선순위
        priority += matchingScoreCalculator.getPositionPriority(
                myRecord.getWantPosition(), otherRecord.getMainPosition(), otherRecord.getSubPosition(), 3, 2, 1);
        priority += matchingScoreCalculator.getPositionPriority(
                otherRecord.getWantPosition(), myRecord.getMainPosition(), myRecord.getSubPosition(), 3, 2, 1);

        // 마이크 우선순위
        priority += matchingScoreCalculator.getMikePriority(myRecord.getMike(), otherRecord.getMike(), 3);

        return priority;
    }

    /**
     * 칼바람 모드 우선순위 계산
     */
    public int calculateAramPriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        int priority = 0;

        // 매너 우선순위
        priority += matchingScoreCalculator.getMannerPriority(
                otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 16, 4);

        // 마이크 우선순위
        priority += matchingScoreCalculator.getMikePriority(myRecord.getMike(), otherRecord.getMike(), 3);

        return priority;
    }

    /**
     * 정밀 매칭 검증 메서드
     */
    public boolean validatePreciseMatching(MatchingRecord myRecord, MatchingRecord otherRecord) {
        // 마이크가 다를 경우 매칭 실패
        if (!myRecord.getMike().equals(otherRecord.getMike())) {
            return false;
        }

        // 내가 원하는 포지션이 상대 포지션이 아닐 경우 매칭 실패
        if (!otherRecord.getMainPosition().equals(myRecord.getWantPosition()) &&
                !otherRecord.getSubPosition().equals(myRecord.getWantPosition())) {
            return false;
        }

        // 티어 차이가 1개 이상 나면 매칭 실패
        return Math.abs(myRecord.getSoloTier().ordinal() - otherRecord.getSoloTier().ordinal()) <= 1;
    }


}
