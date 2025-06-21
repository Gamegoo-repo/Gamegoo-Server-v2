package com.gamegoo.gamegoo_v2.matching.service;

import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingStrategyProcessor {

    /**
     * 정밀매칭 우선순위 계산
     *
     * @param myRecord    내 매칭 정보
     * @param otherRecord 상대방 매칭 정보
     * @return 우선순위 점수
     */
    public int calculatePrecisePriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        switch (myRecord.getGameMode()) {
            case FAST -> {
                return MatchingScoreCalculator.getMannerPriority(
                        otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 25, 4);
            }
            case SOLO -> {
                return MatchingScoreCalculator.getMannerPriority(
                        otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 67, 4);
            }
            case FREE -> {
                return MatchingScoreCalculator.getMannerPriority(
                        otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 65, 4);
            }
            case ARAM -> {
                return MatchingScoreCalculator.getMannerPriority(
                        otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 19, 4);
            }
        }
        return 0;
    }

    /**
     * 빠른대전 우선순위 계산
     *
     * @param myRecord    내 매칭 정보
     * @param otherRecord 상대방 매칭 정보
     * @return 우선순위 점수
     */
    public int calculateFastPriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        int priority = 0;

        // 매너 우선순위
        priority += MatchingScoreCalculator.getMannerPriority(
                otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 16, 4);

        // 포지션 우선순위
        priority += MatchingScoreCalculator.getPositionPriority(
                myRecord.getWantP(), otherRecord.getMainP(), otherRecord.getSubP(), 3, 2, 1);
        priority += MatchingScoreCalculator.getPositionPriority(
                otherRecord.getWantP(), myRecord.getMainP(), myRecord.getSubP(), 3, 2, 1);

        // 마이크 우선순위
        priority += MatchingScoreCalculator.getMikePriority(myRecord.getMike(), otherRecord.getMike(), 3);

        return priority;
    }

    /**
     * 개인랭크 모드 우선순위 계산
     *
     * @param myRecord    내 매칭 정보
     * @param otherRecord 상대방 매칭 정보
     * @return 우선순위 점수
     */
    public int calculateSoloPriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        int priority = 0;

        // 매너 우선순위
        priority += MatchingScoreCalculator.getMannerPriority(
                otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 16, 4);

        // 티어 및 랭킹 점수 계산
        priority += MatchingScoreCalculator.getTierRankPriority(
                myRecord.getTier(), myRecord.getGameRank(),
                otherRecord.getTier(), otherRecord.getGameRank(), 40, 4);

        // 포지션 우선순위
        priority += MatchingScoreCalculator.getPositionPriority(
                myRecord.getWantP(), otherRecord.getMainP(), otherRecord.getSubP(), 3, 2, 1);
        priority += MatchingScoreCalculator.getPositionPriority(
                otherRecord.getWantP(), myRecord.getMainP(), myRecord.getSubP(), 3, 2, 1);

        // 마이크 우선순위
        priority += MatchingScoreCalculator.getMikePriority(myRecord.getMike(), otherRecord.getMike(), 5);

        return priority;
    }

    /**
     * 자유랭크 모드 우선순위 계산
     *
     * @param myRecord    내 매칭 정보
     * @param otherRecord 상대방 매칭 정보
     * @return 우선순위 점수
     */
    public int calculateFreePriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        int priority = 0;

        // 매너 우선순위
        priority += MatchingScoreCalculator.getMannerPriority(
                otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 16, 4);

        // 티어 및 랭킹 점수 계산
        priority += MatchingScoreCalculator.getTierRankPriority(
                myRecord.getTier(), myRecord.getGameRank(),
                otherRecord.getTier(), otherRecord.getGameRank(), 40, 4);

        // 포지션 우선순위
        priority += MatchingScoreCalculator.getPositionPriority(
                myRecord.getWantP(), otherRecord.getMainP(), otherRecord.getSubP(), 3, 2, 1);
        priority += MatchingScoreCalculator.getPositionPriority(
                otherRecord.getWantP(), myRecord.getMainP(), myRecord.getSubP(), 3, 2, 1);

        // 마이크 우선순위
        priority += MatchingScoreCalculator.getMikePriority(myRecord.getMike(), otherRecord.getMike(), 3);

        return priority;
    }

    /**
     * 칼바람 모드 우선순위 계산
     *
     * @param myRecord    내 매칭 정보
     * @param otherRecord 상대방 매칭 정보
     * @return 우선순위 점수
     */
    public int calculateAramPriority(MatchingRecord myRecord, MatchingRecord otherRecord) {
        int priority = 0;

        // 매너 우선순위
        priority += MatchingScoreCalculator.getMannerPriority(
                otherRecord.getMannerLevel(), myRecord.getMannerLevel(), 16, 4);

        // 마이크 우선순위
        priority += MatchingScoreCalculator.getMikePriority(myRecord.getMike(), otherRecord.getMike(), 3);

        return priority;
    }

    /**
     * 정밀 매칭 검증
     *
     * @param myRecord    내 매칭 정보
     * @param otherRecord 상대방 매칭 정보
     * @return 매칭 가능 여부
     */
    public boolean validatePreciseMatching(MatchingRecord myRecord, MatchingRecord otherRecord) {
        // 마이크가 다를 경우 매칭 실패
        if (!myRecord.getMike().equals(otherRecord.getMike())) {
            return false;
        }

        // 내가 원하는 포지션이 상대 포지션이 아닐 경우 매칭 실패
        boolean matches = myRecord.getWantP().isEmpty() ||
                myRecord.getWantP().contains(otherRecord.getMainP()) ||
                myRecord.getWantP().contains(otherRecord.getSubP());
        if (!matches) {
            return false;
        }

        // 티어 차이가 1개 이상 나면 매칭 실패
        return Math.abs(myRecord.getTier().ordinal() - otherRecord.getTier().ordinal()) < 1;
    }


}
