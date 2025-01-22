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
     * FAST 모드 우선순위 계산
     */
    public int calculateFastPriority(Position myWantPosition, Position otherMainPosition, Position otherSubPosition,
                                     Mike myMike, Mike otherMike, Integer myManner, Integer otherManner,
                                     Tier myTier, Integer myRank, Tier otherTier, Integer otherRank) {
        int priority = 0;

        // 포지션 우선순위
        priority += matchingPriorityEvaluateService.getPositionPriority(myWantPosition, otherMainPosition,
                otherSubPosition, 5, 3, 1);

        // 마이크 우선순위
        priority += matchingPriorityEvaluateService.getMikePriority(myMike, otherMike, 5, 3);

        // 매너 우선순위
        priority += matchingPriorityEvaluateService.getMannerPriority(otherManner, myManner, 10, 2);

        // 티어 및 랭킹 우선순위
        priority += matchingPriorityEvaluateService.getTierRankPriority(myTier, myRank, otherTier, otherRank, 50, 5,
                10);

        return priority;
    }

    /**
     * SOLO 모드 우선순위 계산
     */
    public int calculateSoloPriority(Position myWantPosition, Position otherMainPosition, Position otherSubPosition,
                                     Mike myMike, Mike otherMike, Integer myManner, Integer otherManner,
                                     Tier myTier, Integer myRank, Tier otherTier, Integer otherRank) {
        int priority = 0;

        // 포지션 우선순위
        priority += matchingPriorityEvaluateService.getPositionPriority(myWantPosition, otherMainPosition,
                otherSubPosition, 8, 4, 2);

        // 마이크 우선순위
        priority += matchingPriorityEvaluateService.getMikePriority(myMike, otherMike, 4, 2);

        // 매너 우선순위
        priority += matchingPriorityEvaluateService.getMannerPriority(otherManner, myManner, 15, 3);

        // 티어 및 랭킹 우선순위
        priority += matchingPriorityEvaluateService.getTierRankPriority(myTier, myRank, otherTier, otherRank, 60, 6,
                12);

        return priority;
    }

    /**
     * FREE 모드 우선순위 계산
     */
    public int calculateFreePriority(Position myWantPosition, Position otherMainPosition, Position otherSubPosition,
                                     Mike myMike, Mike otherMike, Integer myManner, Integer otherManner,
                                     Tier myTier, Integer myRank, Tier otherTier, Integer otherRank) {
        int priority = 0;

        // 포지션 우선순위
        priority += matchingPriorityEvaluateService.getPositionPriority(myWantPosition, otherMainPosition,
                otherSubPosition, 6, 3, 1);

        // 마이크 우선순위
        priority += matchingPriorityEvaluateService.getMikePriority(myMike, otherMike, 6, 4);

        // 매너 우선순위
        priority += matchingPriorityEvaluateService.getMannerPriority(otherManner, myManner, 12, 2);

        // 티어 및 랭킹 우선순위
        priority += matchingPriorityEvaluateService.getTierRankPriority(myTier, myRank, otherTier, otherRank, 45, 4, 9);

        return priority;
    }

    /**
     * ARAM 모드 우선순위 계산
     */
    public int calculateAramPriority(Position myWantPosition, Position otherMainPosition, Position otherSubPosition,
                                     Mike myMike, Mike otherMike, Integer myManner, Integer otherManner,
                                     Tier myTier, Integer myRank, Tier otherTier, Integer otherRank) {
        int priority = 0;

        // 포지션 우선순위
        priority += matchingPriorityEvaluateService.getPositionPriority(myWantPosition, otherMainPosition,
                otherSubPosition, 4, 2, 1);

        // 마이크 우선순위
        priority += matchingPriorityEvaluateService.getMikePriority(myMike, otherMike, 3, 2);

        // 매너 우선순위
        priority += matchingPriorityEvaluateService.getMannerPriority(otherManner, myManner, 8, 1);

        // 티어 및 랭킹 우선순위
        priority += matchingPriorityEvaluateService.getTierRankPriority(myTier, myRank, otherTier, otherRank, 30, 3, 6);

        return priority;
    }

}
