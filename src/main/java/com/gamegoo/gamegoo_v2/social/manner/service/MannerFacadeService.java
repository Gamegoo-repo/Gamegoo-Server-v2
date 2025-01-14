package com.gamegoo.gamegoo_v2.social.manner.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerInsertRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerUpdateRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerInsertResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerKeywordResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerRatingResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MannerFacadeService {

    private final MemberService memberService;
    private final MannerService mannerService;

    /**
     * 매너 평가 등록 facade 메소드
     *
     * @param member         회원
     * @param targetMemberId 상대 회원 id
     * @param request        매너 평가 요청
     * @return MannerInsertResponse
     */
    @Transactional
    public MannerInsertResponse insertPositiveMannerRating(Member member, Long targetMemberId,
                                                           MannerInsertRequest request) {
        Member targetMember = memberService.findMemberById(targetMemberId);

        // 매너 평가 등록
        MannerRating mannerRating = mannerService.insertMannerRating(member, targetMember,
                request.getMannerKeywordIdList(), true);

        // 매너 점수 및 레벨 업데이트
        mannerService.updateMannerScoreAndLevel(targetMember, request.getMannerKeywordIdList().size());

        return MannerInsertResponse.of(mannerRating, request.getMannerKeywordIdList());
    }

    /**
     * 비매너 평가 등록 facade 메소드
     *
     * @param member         회원
     * @param targetMemberId 상대 회원 id
     * @param request        매너 평가 요청
     * @return MannerInsertResponse
     */
    @Transactional
    public MannerInsertResponse insertNegativeMannerRating(Member member, Long targetMemberId,
                                                           MannerInsertRequest request) {
        Member targetMember = memberService.findMemberById(targetMemberId);

        // 비매너 평가 등록
        MannerRating mannerRating = mannerService.insertMannerRating(member, targetMember,
                request.getMannerKeywordIdList(), false);

        // 매너 점수 및 레벨 업데이트
        int score = -2 * request.getMannerKeywordIdList().size();
        mannerService.updateMannerScoreAndLevel(targetMember, score);

        return MannerInsertResponse.of(mannerRating, request.getMannerKeywordIdList());
    }

    /**
     * 매너/비매너 평가 수정 facade 메소드
     *
     * @param member   회원
     * @param mannerId 매너 평가 id
     * @param request  매너 평가 요청
     * @return MannerUpdateResponse
     */
    @Transactional
    public MannerUpdateResponse updateMannerRating(Member member, Long mannerId, MannerUpdateRequest request) {
        MannerRating mannerRating = mannerService.getMannerRatingById(mannerId);

        int beforeSize = mannerRating.getMannerRatingKeywordList().size();

        // 매너/비매너 평가 업데이트
        MannerRating updatedMannerRating = mannerService.updateMannerRating(member, mannerRating,
                request.getMannerKeywordIdList());

        int afterSize = updatedMannerRating.getMannerRatingKeywordList().size();

        // 매너 점수 및 레벨 업데이트
        int score = afterSize - beforeSize;
        if (!mannerRating.isPositive()) {
            score = -score;
        }
        mannerService.updateMannerScoreAndLevel(mannerRating.getToMember(), score);

        return MannerUpdateResponse.of(updatedMannerRating, request.getMannerKeywordIdList());
    }

    /**
     * 상대에게 남긴 매너 평가 정보 조회 facade 메소드
     *
     * @param member         회원
     * @param targetMemberId 대상 회원 id
     * @param positive       매너/비매너 평가 여부
     * @return MannerRatingResponse
     */
    public MannerRatingResponse getMannerRating(Member member, Long targetMemberId, boolean positive) {
        Member targetMember = memberService.findMemberById(targetMemberId);
        Optional<MannerRating> mannerRating = mannerService.getMannerRatingByMember(member, targetMember, positive);

        return MannerRatingResponse.of(mannerRating);
    }

    /**
     * 해당 회원의 매너 정보 조회 facade 메소드
     *
     * @param memberId 회원 id
     * @return MannerResponse
     */
    public MannerResponse getMannerInfo(Long memberId) {
        Member member = memberService.findMemberById(memberId);

        // 매너 레벨 조회
        int mannerLevel = member.getMannerLevel();

        // 매너 랭크 조회
        Double mannerRank = member.getMannerRank();

        // 매너 평가 개수 조회
        int mannerRatingCount = mannerService.countMannerRatingByMember(member, true);

        // 매너 키워드별 받은 개수 조회
        Map<Long, Integer> mannerKeywordMap = mannerService.countMannerKeyword(member);

        List<MannerKeywordResponse> mannerKeywordResponses = mannerKeywordMap.entrySet().stream()
                .map(entry -> MannerKeywordResponse.of(entry.getKey(), entry.getValue()))
                .toList();

        return MannerResponse.of(mannerLevel, mannerRank, mannerRatingCount, mannerKeywordResponses);
    }

}
