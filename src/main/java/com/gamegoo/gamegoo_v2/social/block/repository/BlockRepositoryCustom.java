package com.gamegoo.gamegoo_v2.social.block.repository;

import java.util.List;
import java.util.Map;

public interface BlockRepositoryCustom {

    /**
     * 상대 회원 각각에 대해 회원의 차단 여부 조회
     *
     * @param targetMemberIds 상대 회원 id list
     * @param memberId        회원 id
     * @return Map<상대 회원 id, 차단 여부>
     */
    Map<Long, Boolean> isBlockedByTargetMembersBatch(List<Long> targetMemberIds, Long memberId);

    /**
     * 회원이 상대 회원들을 차단했는지 여부 조회
     * @param targetMemberIds 상대 회원 id list
     * @param memberId 회원 id
     * @return Map<상대 회원 id, 회원이 해당 상대를 차단했는지 여부>
     */
    Map<Long, Boolean> hasBlockedTargetMembersBatch(List<Long> targetMemberIds, Long memberId);

}
