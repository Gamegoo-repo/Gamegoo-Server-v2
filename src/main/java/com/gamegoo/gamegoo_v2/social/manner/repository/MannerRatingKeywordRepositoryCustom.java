package com.gamegoo.gamegoo_v2.social.manner.repository;

import java.util.Map;

public interface MannerRatingKeywordRepositoryCustom {

    /**
     * 해당 회원이 받은 매너 키워드 별 개수 조회
     *
     * @param memberId 회원 id
     * @return Map<매너 키워드 id, 개수>
     */
    Map<Long, Integer> countMannerKeywordByToMemberId(Long memberId);

}
