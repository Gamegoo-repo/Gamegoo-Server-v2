package com.gamegoo.gamegoo_v2.matching.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingPriorityCalculateService {

    private final MatchingPriorityEvaluateService matchingPriorityEvaluateService;

    /**
     * 정밀 매칭 우선순위 계산
     */
    public int calculatePrecisePriority(Integer myManner, Integer otherManner) {
        return matchingPriorityEvaluateService.getMannerPriority(otherManner, myManner, 16, 4);
    }

    /**
     * 빠른대전 우선순위 계산
     */
    public int calculateFastPriority(Position myMainP, Position mySubP, Position myWantP,
                                     Position otherMainP, Position otherSubP, Position otherWantP,
                                     Mike myMike, Mike otherMike, Integer myManner, Integer otherManner,
                                     Tier mySoloTier, Integer mySoloRank, Tier myFreeTier, Integer myFreeRank,
                                     Tier otherSoloTier, Integer otherSoloRank, Tier otherFreeTier,
                                     Integer otherFreeRank) {
        int priority = 0;

        // 매너 우선순위
        priority += matchingPriorityEvaluateService.getMannerPriority(otherManner, myManner, 16, 4);

        // TODO: 티어 및 랭킹 점수 계산
        // priority += matchingPriorityEvaluateService.getTierRankPriority(myTier, myRank, otherTier, otherRank, 40, 4);

        // 포지션 우선순위
        priority += matchingPriorityEvaluateService.getPositionPriority(myWantP, otherMainP, otherSubP, 3, 2, 1);
        priority += matchingPriorityEvaluateService.getPositionPriority(otherWantP, myMainP, mySubP, 3, 2, 1);

        // 마이크 우선순위
        priority += matchingPriorityEvaluateService.getMikePriority(myMike, otherMike, 3);

        return priority;
    }

    /**
     * 개인 랭크 모드 우선순위 계산
     */
    public int calculateSoloPriority(Position myMainP, Position mySubP, Position myWantP,
                                     Position otherMainP, Position otherSubP, Position otherWantP,
                                     Mike myMike, Mike otherMike, Integer myManner, Integer otherManner,
                                     Tier myTier, Integer myRank, Tier otherTier, Integer otherRank) {
        // 티어 및 랭킹 제한 확인
        if (!validateSoloRankRange(myTier, otherTier)) {
            return 0;
        }

        int priority = 0;

        // 매너 우선순위
        priority += matchingPriorityEvaluateService.getMannerPriority(otherManner, myManner, 16, 4);

        // 티어 및 랭킹 점수 계산
        priority += matchingPriorityEvaluateService.getTierRankPriority(myTier, myRank, otherTier, otherRank, 40, 4);

        // 포지션 우선순위
        priority += matchingPriorityEvaluateService.getPositionPriority(myWantP, otherMainP, otherSubP, 3, 2, 1);
        priority += matchingPriorityEvaluateService.getPositionPriority(otherWantP, myMainP, mySubP, 3, 2, 1);

        // 마이크 우선순위
        priority += matchingPriorityEvaluateService.getMikePriority(myMike, otherMike, 5);

        return priority;
    }

    /**
     * FREE 모드 우선순위 계산
     */
    public int calculateFreePriority(Position myMainP, Position mySubP, Position myWantP,
                                     Position otherMainP, Position otherSubP, Position otherWantP,
                                     Mike myMike, Mike otherMike, Integer myManner, Integer otherManner,
                                     Tier myTier, Integer myRank, Tier otherTier, Integer otherRank) {
        // 티어 및 랭킹 제한 확인
        if (myTier.ordinal() < 5 && otherTier.ordinal() >= 6) {
            return 0;
        }

        int priority = 0;

        // 매너 우선순위
        priority += matchingPriorityEvaluateService.getMannerPriority(otherManner, myManner, 16, 4);

        // 티어 및 랭킹 점수 계산
        priority += matchingPriorityEvaluateService.getTierRankPriority(myTier, myRank, otherTier, otherRank, 40, 4);

        // 포지션 우선순위
        priority += matchingPriorityEvaluateService.getPositionPriority(myWantP, otherMainP, otherSubP, 3, 2, 1);
        priority += matchingPriorityEvaluateService.getPositionPriority(otherWantP, myMainP, mySubP, 3, 2, 1);

        // 마이크 우선순위
        priority += matchingPriorityEvaluateService.getMikePriority(myMike, otherMike, 3);

        return priority;
    }

    /**
     * 칼바람 모드 우선순위 계산
     */
    public int calculateAramPriority(Position myWantPosition, Position otherMainPosition, Position otherSubPosition,
                                     Mike myMike, Mike otherMike, Integer myManner, Integer otherManner,
                                     Tier myTier, Integer myRank, Tier otherTier, Integer otherRank) {
        int priority = 0;

        // 매너 우선순위
        priority += matchingPriorityEvaluateService.getMannerPriority(otherManner, myManner, 16, 4);

        // 마이크 우선순위
        priority += matchingPriorityEvaluateService.getMikePriority(myMike, otherMike, 3);

        return priority;
    }

    /**
     * 정밀 매칭 검증 메서드
     */
    public boolean validatePreciseMatching(Mike myMike, Mike otherMike, Position myWantPosition,
                                           Position otherMainPosition, Position otherSubPosition,
                                           Tier myTier, Tier otherTier) {
        // 마이크가 다를 경우 매칭 실패
        if (!myMike.equals(otherMike)) {
            return false;
        }

        // 내가 원하는 포지션이 상대 포지션이 아닐 경우 매칭 실패
        if (!otherMainPosition.equals(myWantPosition) && !otherSubPosition.equals(myWantPosition)) {
            return false;
        }

        // 티어 차이가 1개 이상 나면 매칭 실패
        return Math.abs(myTier.ordinal() - otherTier.ordinal()) <= 1;
    }

    /**
     * 개인 랭크 제한 검증
     */
    private boolean validateSoloRankRange(Tier myTier, Tier otherTier) {
        int[][] allowedRanges = {
                {0, 1, 2}, // 아이언
                {0, 1, 2}, // 브론즈
                {0, 1, 2, 3}, // 실버
                {2, 3, 4}, // 골드
                {3, 4, 5}, // 플레티넘
                {4, 5, 6}, // 에메랄드
                {5, 6} // 다이아몬드
        };

        int myTierIndex = myTier.ordinal();
        int otherTierIndex = otherTier.ordinal();

        if (myTierIndex >= allowedRanges.length) {
            return false; // 마스터 이상은 제외
        }

        for (int tier : allowedRanges[myTierIndex]) {
            if (tier == otherTierIndex) {
                return true;
            }
        }

        return false;
    }

}
