package com.gamegoo.gamegoo_v2.social.manner.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.validator.MemberValidator;
import com.gamegoo.gamegoo_v2.core.event.MannerLevelDownEvent;
import com.gamegoo.gamegoo_v2.core.event.MannerLevelUpEvent;
import com.gamegoo.gamegoo_v2.core.event.MannerRatingInsertEvent;
import com.gamegoo.gamegoo_v2.core.exception.MannerException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRatingKeyword;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerRatingKeywordRepository;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MannerService {

    private final MemberValidator memberValidator;
    private final MannerRatingRepository mannerRatingRepository;
    private final MannerKeywordRepository mannerKeywordRepository;
    private final MannerRatingKeywordRepository mannerRatingKeywordRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final int MANNER_KEYWORD_ID_MAX = 12;
    private final int MANNER_KEYWORD_ID_MIN = 1;

    /**
     * 매너 평가 등록 메소드
     *
     * @param member              회원
     * @param targetMember        대상 회원
     * @param mannerKeywordIdList 매너키워드 id list
     * @param isPositive          매너/비매너 평가 여부
     * @return MannerRating
     */
    @Transactional
    public MannerRating insertMannerRating(Member member, Member targetMember, List<Long> mannerKeywordIdList,
                                           boolean isPositive) {
        // 매너 키워드 검증
        validateMannerInsertRequest(mannerKeywordIdList, isPositive);

        // targetMember로 나 자신을 요청한 경우 검증
        memberValidator.throwIfEqual(member, targetMember);

        // 상대방의 탈퇴 여부 검증
        memberValidator.throwIfBlind(targetMember);

        // 매너 평가 최초 여부 검증
        validateMannerRatingNotExists(member, targetMember, isPositive);

        // 매너 키워드 엔티티 조회
        List<MannerKeyword> mannerKeywordList = getMannerKeywordList(mannerKeywordIdList);

        // MannerRating 엔티티 생성 및 저장
        MannerRating mannerRating = MannerRating.create(member, targetMember, isPositive);
        mannerRatingRepository.save(mannerRating);

        // MannerRatingKeyword 엔티티 생성 및 저장
        List<MannerRatingKeyword> mannerRatingKeywordList = mannerKeywordList.stream()
                .map(mannerKeyword -> MannerRatingKeyword.create(mannerRating, mannerKeyword))
                .toList();
        mannerRatingKeywordRepository.saveAll(mannerRatingKeywordList);

        // 매너평가 등록됨 알림 생성
        eventPublisher.publishEvent(new MannerRatingInsertEvent(member.getId(), mannerKeywordIdList));

        return mannerRating;
    }

    /**
     * 회원의 매너 점수 및 매너 레벨 업데이트
     *
     * @param member 회원
     * @param score  매너 점수 증가/감소량
     */
    @Transactional
    public void updateMannerScoreAndLevel(Member member, int score) {
        // 매너 점수 업데이트
        int currentScore = member.getMannerScore() != null ? member.getMannerScore() : 0;
        Integer updatedScore = member.updateMannerScore(currentScore + score);

        // 매너 레벨 업데이트
        int currentLevel = member.getMannerLevel();
        int updatedLevel = member.updateMannerLevel(calculateMannerLevel(updatedScore));

        // 매너 레벨 알림 생성
        if (currentLevel < updatedLevel) { // 매너 레벨 상승한 경우
            eventPublisher.publishEvent(new MannerLevelUpEvent(member.getId(), updatedLevel));
        } else if (currentLevel > updatedLevel) { // 매너 레벨 하락한 경우
            eventPublisher.publishEvent(new MannerLevelDownEvent(member.getId(), updatedLevel));
        }
    }

    /**
     * id에 해당하는 매너 키워드 list 조회
     *
     * @param mannerKeywordIdList 매너 키워드 id list
     * @return 매너 키워드 list
     */
    private List<MannerKeyword> getMannerKeywordList(List<Long> mannerKeywordIdList) {
        return mannerKeywordIdList.stream()
                .map(mannerKeywordId -> mannerKeywordRepository.findById(mannerKeywordId)
                        .orElseThrow(() -> new MannerException(ErrorCode.MANNER_KEYWORD_NOT_FOUND))
                ).toList();
    }

    /**
     * 매너 키워드 id 값 검증
     *
     * @param mannerKeywordIdList 매너 키워드 id list
     * @param isPositive          매너/비매너 평가 여부
     */
    private void validateMannerInsertRequest(List<Long> mannerKeywordIdList, boolean isPositive) {
        boolean notValid;

        if (isPositive) {
            notValid = mannerKeywordIdList.stream().anyMatch(id -> id >= 7 || id < MANNER_KEYWORD_ID_MIN);
        } else {
            notValid = mannerKeywordIdList.stream().anyMatch(id -> id < 7 || id > MANNER_KEYWORD_ID_MAX);
        }

        if (notValid) {
            throw new MannerException(ErrorCode.MANNER_KEYWORD_INVALID);
        }
    }

    /**
     * 회원이 상대 회원에게 등록한 매너 평가가 있는 경우 예외 발생
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @param isPositive   매너/비매너평가 여부
     */
    private void validateMannerRatingNotExists(Member member, Member targetMember, boolean isPositive) {
        boolean exists = mannerRatingRepository.existsByFromMemberIdAndToMemberIdAndPositive(member.getId(),
                targetMember.getId(), isPositive);
        if (exists) {
            throw new MannerException(ErrorCode.MANNER_RATING_EXISTS);
        }
    }

    /**
     * 매너 점수를 기반으로 매너 레벨 계산
     *
     * @param mannerScore 매너 점수
     * @return 매너 레벨
     */
    private int calculateMannerLevel(int mannerScore) {
        if (mannerScore < 10) {
            return 1;
        } else if (mannerScore < 20) {
            return 2;
        } else if (mannerScore < 30) {
            return 3;
        } else if (mannerScore < 40) {
            return 4;
        } else {
            return 5;
        }
    }

}
