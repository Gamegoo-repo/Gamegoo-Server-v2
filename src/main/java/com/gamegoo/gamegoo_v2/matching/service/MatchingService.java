package com.gamegoo.gamegoo_v2.matching.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.dto.PriorityValue;
import com.gamegoo.gamegoo_v2.matching.dto.response.PriorityListResponse;
import com.gamegoo.gamegoo_v2.matching.repository.MatchingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final MatchingPriorityCalculateService matchingPriorityCalculateService;
    private final MatchingRecordRepository matchingRecordRepository;

    public PriorityListResponse calculatePriorityList(MatchingRecord myMatchingRecord,
                                                      List<MatchingRecord> otherMatchingRecords) {
        // 우선순위 리스트 초기화
        List<PriorityValue> myPriorityList = new ArrayList<>();
        List<PriorityValue> otherPriorityList = new ArrayList<>();

        for (MatchingRecord otherRecord : otherMatchingRecords) {
            Long otherMemberId = otherRecord.getMember().getId();

            // 자신이 아닌 다른 멤버와 비교
            if (!myMatchingRecord.getMember().getId().equals(otherMemberId)) {
                // 나의 우선순위 계산
                int otherPriority = calculatePriority(myMatchingRecord.getGameMode(), myMatchingRecord, otherRecord);
                myPriorityList.add(PriorityValue.of(otherMemberId, otherRecord.getMatchingUuid(), otherPriority));

                // 상대방 관점에서 나의 우선순위 계산
                int myPriority = calculatePriority(myMatchingRecord.getGameMode(), otherRecord, myMatchingRecord);
                otherPriorityList.add(PriorityValue.of(myMatchingRecord.getMember().getId(),
                        myMatchingRecord.getMatchingUuid(), myPriority));
            }
        }

        // PriorityListResponse 반환
        return PriorityListResponse.of(myPriorityList, otherPriorityList, myMatchingRecord.getMember(),
                myMatchingRecord.getMatchingUuid());
    }


    public int calculatePriority(GameMode gameMode, MatchingRecord myRecord, MatchingRecord otherRecord) {
        // 공통 조건
        if (!matchingPriorityCalculateService.validateMatching(myRecord.getMainPosition(), myRecord.getSubPosition(),
                myRecord.getWantPosition(), otherRecord.getMainPosition(), otherRecord.getSubPosition(),
                otherRecord.getWantPosition())) {
            return 0;
        }

        // 정밀 매칭
        if (myRecord.getMatchingType() == MatchingType.PRECISE) {
            if (matchingPriorityCalculateService.validatePreciseMatching(myRecord.getMike(), otherRecord.getMike(),
                    myRecord.getWantPosition(), otherRecord.getMainPosition(), otherRecord.getSubPosition(),
                    myRecord.getSoloTier(), otherRecord.getSoloTier())) {
                return matchingPriorityCalculateService.calculatePrecisePriority(myRecord.getMannerLevel(),
                        otherRecord.getMannerLevel());
            }
            return 0;
        }

        // 겜구 매칭
        return switch (gameMode) {
            case SOLO ->
                // 개인 랭크 모드 우선순위 계산
                    matchingPriorityCalculateService.calculateSoloPriority(
                            myRecord.getMainPosition(),
                            myRecord.getSubPosition(),
                            myRecord.getWantPosition(),
                            otherRecord.getMainPosition(),
                            otherRecord.getSubPosition(),
                            otherRecord.getWantPosition(),
                            myRecord.getMike(),
                            otherRecord.getMike(),
                            myRecord.getMannerLevel(),
                            otherRecord.getMannerLevel(),
                            myRecord.getSoloTier(),
                            myRecord.getSoloRank(),
                            otherRecord.getSoloTier(),
                            otherRecord.getSoloRank()
                    );
            case FREE ->
                // 자유 랭크 모드 우선순위 계산
                    matchingPriorityCalculateService.calculateFreePriority(
                            myRecord.getMainPosition(),
                            myRecord.getSubPosition(),
                            myRecord.getWantPosition(),
                            otherRecord.getMainPosition(),
                            otherRecord.getSubPosition(),
                            otherRecord.getWantPosition(),
                            myRecord.getMike(),
                            otherRecord.getMike(),
                            myRecord.getMannerLevel(),
                            otherRecord.getMannerLevel(),
                            myRecord.getFreeTier(),
                            myRecord.getFreeRank(),
                            otherRecord.getFreeTier(),
                            otherRecord.getFreeRank()
                    );
            case ARAM ->
                // 칼바람 모드 우선순위 계산
                    matchingPriorityCalculateService.calculateAramPriority(
                            myRecord.getMike(),
                            otherRecord.getMike(),
                            myRecord.getMannerLevel(),
                            otherRecord.getMannerLevel());
            case FAST ->
                // 빠른대전 우선순위 계산
                    matchingPriorityCalculateService.calculateFastPriority(
                            myRecord.getMainPosition(),
                            myRecord.getSubPosition(),
                            myRecord.getWantPosition(),
                            otherRecord.getMainPosition(),
                            otherRecord.getSubPosition(),
                            otherRecord.getWantPosition(),
                            myRecord.getMike(),
                            otherRecord.getMike(),
                            myRecord.getMannerLevel(),
                            otherRecord.getMannerLevel(),
                            myRecord.getSoloTier(),
                            myRecord.getSoloRank(),
                            myRecord.getFreeTier(),
                            myRecord.getFreeRank(),
                            otherRecord.getSoloTier(),
                            otherRecord.getSoloRank(),
                            otherRecord.getFreeTier(),
                            otherRecord.getFreeRank()
                    );
        };
    }


    /**
     * 매칭 대기 중인 Matching Records List 조회
     *
     * @param gameMode 게임모드
     * @return 대기 중인 매칭 리스트
     */
    public List<MatchingRecord> getPENDINGMatchingRecords(GameMode gameMode) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        return matchingRecordRepository.findMatchingRecordsWithGroupBy(
                fiveMinutesAgo,
                MatchingStatus.PENDING,
                gameMode
        );

    }

    /**
     * 매칭기록 생성 및 DB 저장
     *
     * @param member       회원
     * @param matchingType 매칭 유형
     * @param gameMode     게임 모드
     * @return 매칭 기록
     */
    public MatchingRecord createMatchingRecord(Member member, MatchingType matchingType, GameMode gameMode) {
        MatchingRecord matchingRecord = MatchingRecord.create(gameMode, matchingType, member);
        return matchingRecordRepository.save(matchingRecord);
    }

}
