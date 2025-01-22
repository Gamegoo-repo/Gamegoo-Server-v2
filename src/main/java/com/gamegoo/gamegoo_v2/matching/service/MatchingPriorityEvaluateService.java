package com.gamegoo.gamegoo_v2.matching.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingPriorityEvaluateService {

    /**
     * 매너레벨 점수 계산
     */
    public int getMannerPriority(Integer otherManner, Integer myManner, int maxMannerPriority,
                                 int mannerDifferenceMultiplier) {
        int mannerDifference = Math.abs(myManner - otherManner);
        return maxMannerPriority - mannerDifference * mannerDifferenceMultiplier;
    }

    /**
     * 랭킹 우선순위 점수 점수 계산
     *
     * @param myTier              내 티어
     * @param myRank              내 랭크
     * @param otherTier           타겟 티어
     * @param otherRank           타겟 랭크
     * @param maxTierRankPriority 최대 점수
     * @param tierMultiplier      티어 점수
     * @param maxRankScore        랭크 점수
     * @return 랭킹 우선순위 값
     */
    public int getTierRankPriority(Tier myTier, Integer myRank, Tier otherTier, Integer otherRank,
                                   int maxTierRankPriority, int tierMultiplier, int maxRankScore) {
        int myScore = getTierRankScore(myTier, myRank, tierMultiplier, maxRankScore);
        int otherScore = getTierRankScore(otherTier, otherRank, tierMultiplier, maxRankScore);
        int scoreDifference = Math.abs(myScore - otherScore);

        return maxTierRankPriority - scoreDifference;
    }

    /**
     * 티어, 랭크 점수 계산
     *
     * @param tier           티어
     * @param rank           랭크
     * @param tierMultiplier 티어 점수
     * @param maxRankScore   랭크 점수
     * @return 랭킹 점수
     */
    private int getTierRankScore(Tier tier, int rank, int tierMultiplier, int maxRankScore) {
        return tier.ordinal() * tierMultiplier + (maxRankScore - rank);
    }

    /**
     * 포지션 우선순위 점수 계산
     *
     * @param myWantPosition          내 주포지션
     * @param otherMainPosition       타겟 주포지션
     * @param otherSubPosition        타겟 부포지션
     * @param mainPositionPriority    높은 추가 점수
     * @param subPositionPriority     중간 추가 점수
     * @param defaultPositionPriority 낮은 추가 점수
     * @return 포지션 우선순위 점수
     */
    public int getPositionPriority(Position myWantPosition, Position otherMainPosition, Position otherSubPosition,
                                   int mainPositionPriority, int subPositionPriority, int defaultPositionPriority) {
        int priority = 0;

        if (myWantPosition == otherMainPosition || myWantPosition == Position.ANY || otherMainPosition == Position.ANY) {
            priority += mainPositionPriority;
        } else if (myWantPosition == otherSubPosition || otherSubPosition == Position.ANY) {
            priority += subPositionPriority;
        } else {
            priority += defaultPositionPriority;
        }

        return priority;
    }

    /**
     * 마이크 우선순위 점수 계산
     *
     * @param myMike                   내 마이크
     * @param otherMike                타겟 마이크
     * @param mikeMatchPriority        마이크 점수
     * @param mikePartialMatchPriority 마이크 부분 점수
     * @return 마이크 우선순위 점수
     */
    public int getMikePriority(Mike myMike, Mike otherMike, int mikeMatchPriority, int mikePartialMatchPriority) {
        if (myMike == Mike.UNAVAILABLE && otherMike == Mike.UNAVAILABLE) {
            return mikeMatchPriority;
        }
        if (myMike == Mike.AVAILABLE && otherMike == Mike.AVAILABLE) {
            return mikeMatchPriority;
        }
        if ((myMike == Mike.AVAILABLE && otherMike == Mike.ONLY_LISTEN) ||
                (myMike == Mike.ONLY_LISTEN && otherMike == Mike.AVAILABLE)) {
            return mikePartialMatchPriority;
        }
        return 0;
    }

}
