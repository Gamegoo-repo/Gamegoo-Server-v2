package com.gamegoo.gamegoo_v2.social.manner.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerInsertRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerInsertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


}
