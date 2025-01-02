package com.gamegoo.gamegoo_v2.social.friend.repository;

import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Map;

public interface FriendRepositoryCustom {

    /**
     * 커서 기반 친구 목록 조회
     *
     * @param memberId 회원 id
     * @param cursor   친구 회원 id
     * @param pageSize 페이지 크기
     * @return 친구 Slice
     */
    Slice<Friend> findFriendsByCursor(Long memberId, Long cursor, int pageSize);

    /**
     * 검색어로 친구 목록 조회
     *
     * @param memberId    회원 id
     * @param queryString 검색어
     * @return 친구 list
     */
    List<Friend> findFriendsByQueryString(Long memberId, String queryString);

    /**
     * 두 회원이 서로 친구인지 여부 조회
     *
     * @param memberId       회원 id
     * @param targetMemberId 상대 회원 id
     * @return 친구 여부
     */
    boolean isFriend(Long memberId, Long targetMemberId);

    /**
     * 상대 회원 각각에 대해 서로 친구인지 여부 조회
     *
     * @param memberId        회원 id
     * @param targetMemberIds 상대 회원 id list
     * @return Map<상대 회원 id, 친구 여부>
     */
    Map<Long, Boolean> isFriendBatch(Long memberId, List<Long> targetMemberIds);

}
