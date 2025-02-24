package com.gamegoo.gamegoo_v2.matching.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.exception.MatchingException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
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

    private final MatchingStrategyProcessor matchingStrategyProcessor;
    private final MatchingRecordRepository matchingRecordRepository;

    /**
     * 매칭 우선순위 리스트 계산 후 조회
     *
     * @param myMatchingRecord     내 매칭 정보
     * @param otherMatchingRecords 상대방 매칭 정보
     * @return 우선순위 계산 API 응답 DTO
     */
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

    /**
     * 우선순위 점수 계산
     *
     * @param gameMode    게임모드
     * @param myRecord    내 매칭 정보
     * @param otherRecord 상대방 매칭 정보
     * @return 우선순위 점수
     */
    public int calculatePriority(GameMode gameMode, MatchingRecord myRecord, MatchingRecord otherRecord) {
        // 정밀 매칭
        if (myRecord.getMatchingType() == MatchingType.PRECISE) {
            if (matchingStrategyProcessor.validatePreciseMatching(myRecord, otherRecord)) {
                return matchingStrategyProcessor.calculatePrecisePriority(myRecord, otherRecord);
            }
            return 0;
        }

        // 겜구 매칭
        return switch (gameMode) {
            case SOLO ->
                // 개인 랭크 모드 우선순위 계산
                    matchingStrategyProcessor.calculateSoloPriority(myRecord, otherRecord);
            case FREE ->
                // 자유 랭크 모드 우선순위 계산
                    matchingStrategyProcessor.calculateFreePriority(myRecord, otherRecord);
            case ARAM ->
                // 칼바람 모드 우선순위 계산
                    matchingStrategyProcessor.calculateAramPriority(myRecord, otherRecord);
            case FAST ->
                // 빠른대전 우선순위 계산
                    matchingStrategyProcessor.calculateFastPriority(myRecord, otherRecord);
        };
    }

    /**
     * 매칭 대기 중인 Matching Records List 조회
     *
     * @param gameMode 게임모드
     * @return 대기 중인 매칭 리스트
     */
    public List<MatchingRecord> getPendingMatchingRecords(GameMode gameMode) {
        return matchingRecordRepository.findValidMatchingRecords(LocalDateTime.now().minusMinutes(5), gameMode);
    }

    /**
     * 매칭기록 생성 및 DB 저장
     *
     * @param member       회원
     * @param matchingType 매칭 유형
     * @param gameMode     게임 모드
     * @return 매칭 기록
     */
    @Transactional
    public MatchingRecord createMatchingRecord(Member member, MatchingType matchingType, GameMode gameMode) {
        MatchingRecord matchingRecord = MatchingRecord.create(gameMode, matchingType, member);
        return matchingRecordRepository.save(matchingRecord);
    }

    /**
     * 가장 최신 매칭 불러오기
     *
     * @param matchingUuid 매칭 uuid
     * @return matchingRecord
     */
    public MatchingRecord getMatchingRecordByMatchingUuid(String matchingUuid) {
        return matchingRecordRepository.findMatchingRecordsByMatchingUuid(matchingUuid).orElseThrow(() -> new MatchingException(ErrorCode.MATCHING_NOT_FOUND));
    }

    /**
     * 나와 연결되어있는 상대방 MatchingRecord 불러오기
     *
     * @param matchingRecord 내 matchingRecord
     * @return matchingRecord 상대방 matchingRecord
     */
    public MatchingRecord getTargetMatchingRecord(MatchingRecord matchingRecord) {
        MatchingRecord targetMatchingRecord = matchingRecord.getTargetMatchingRecord();

        if (targetMatchingRecord == null) {
            throw new MatchingException(ErrorCode.TARGET_MATCHING_MEMBER_NOT_FOUND);
        }

        return targetMatchingRecord;
    }


    /**
     * 매칭 status 변경
     *
     * @param matchingStatus 변경된 status 값
     * @param matchingRecord 변경될 matchingRecord
     */
    @Transactional
    public void setMatchingStatus(MatchingStatus matchingStatus, MatchingRecord matchingRecord) {
        matchingRecord.updateStatus(matchingStatus);
    }

    /**
     * targetMatchingRecord 지정
     *
     * @param matchingRecord       내 matchingRecord
     * @param targetMatchingRecord 상대방 matchingRecord
     */
    @Transactional
    public void setTargetMatchingRecord(MatchingRecord matchingRecord, MatchingRecord targetMatchingRecord) {
        matchingRecord.updateTargetMatchingRecord(targetMatchingRecord);
        targetMatchingRecord.updateTargetMatchingRecord(matchingRecord);
    }

}
