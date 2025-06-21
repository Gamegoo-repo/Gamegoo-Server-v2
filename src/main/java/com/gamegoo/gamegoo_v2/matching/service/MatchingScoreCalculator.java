package com.gamegoo.gamegoo_v2.matching.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;

import java.util.List;

public class MatchingScoreCalculator {

    /**
     * 매너레벨 점수 계산
     *
     * @param otherManner                상대방 매너 점수
     * @param myManner                   내 매너 점수
     * @param maxMannerPriority          매너 점수 최대 우선순위 값
     * @param mannerDifferenceMultiplier 매너 점수 별 가중치 값
     * @return 최종 매너점수
     */
    public static int getMannerPriority(Integer otherManner, Integer myManner, int maxMannerPriority,
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
     * @return 랭킹 우선순위 값
     */
    public static int getTierRankPriority(Tier myTier, Integer myRank, Tier otherTier, Integer otherRank,
                                          int maxTierRankPriority, int tierMultiplier) {
        int myScore = getTierRankScore(myTier, myRank, tierMultiplier);
        int otherScore = getTierRankScore(otherTier, otherRank, tierMultiplier);
        int scoreDifference = Math.abs(myScore - otherScore);

        return maxTierRankPriority - scoreDifference;
    }

    /**
     * 티어, 랭크 점수 계산
     *
     * @param tier           티어
     * @param rank           랭크
     * @param tierMultiplier 티어 점수
     * @return 랭킹 점수
     */
    private static int getTierRankScore(Tier tier, int rank, int tierMultiplier) {
        return tier.ordinal() * tierMultiplier - rank;
    }

    /**
     * 포지션 우선순위 점수 계산
     *
     * @param myWantPositions         내가 원하는 포지션 리스트
     * @param otherMainPosition       타겟 주포지션
     * @param otherSubPosition        타겟 부포지션
     * @param mainPositionPriority    높은 추가 점수
     * @param subPositionPriority     중간 추가 점수
     * @param defaultPositionPriority 낮은 추가 점수
     * @return 포지션 우선순위 점수
     */
    public static int getPositionPriority(List<Position> myWantPositions, Position otherMainPosition,
                                          Position otherSubPosition,
                                          int mainPositionPriority, int subPositionPriority,
                                          int defaultPositionPriority) {
        if (myWantPositions == null || myWantPositions.isEmpty()) {
            return mainPositionPriority;
        }

        int maxPriority = 0;
        for (Position wantPosition : myWantPositions) {
            int currentPriority;
            if (wantPosition == otherMainPosition || wantPosition == Position.ANY || otherMainPosition == Position.ANY) {
                currentPriority = mainPositionPriority;
            } else if (wantPosition == otherSubPosition || otherSubPosition == Position.ANY) {
                currentPriority = subPositionPriority;
            } else {
                currentPriority = defaultPositionPriority;
            }
            maxPriority = Math.max(maxPriority, currentPriority);
        }

        return maxPriority;
    }

    /**
     * 마이크 우선순위 점수 계산
     *
     * @param myMike            내 마이크
     * @param otherMike         타겟 마이크
     * @param mikeMatchPriority 마이크 점수
     * @return 마이크 우선순위 점수
     */
    public static int getMikePriority(Mike myMike, Mike otherMike, int mikeMatchPriority) {
        if (!myMike.equals(otherMike)) {
            return 0;
        }
        return mikeMatchPriority;
    }

}
