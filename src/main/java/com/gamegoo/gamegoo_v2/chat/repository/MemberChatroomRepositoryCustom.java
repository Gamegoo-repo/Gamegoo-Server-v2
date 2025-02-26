package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;

import java.util.List;
import java.util.Map;

public interface MemberChatroomRepositoryCustom {

    /**
     * 채팅방 각각에 대해 상대 회원 조회
     *
     * @param chatroomIds 채팅방 id list
     * @param memberId    회원 id
     * @return Map<채팅방 id, 상대 회원 객체>
     */
    Map<Long, Member> findTargetMembersBatch(List<Long> chatroomIds, Long memberId);

}
