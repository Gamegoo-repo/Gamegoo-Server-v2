package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;

import java.util.List;
import java.util.Optional;

public interface ChatroomRepositoryCustom {

    /**
     * 두 회원 사이의 채팅방 조회
     *
     * @param memberId1 회원
     * @param memberId2 회원
     * @return 채팅방 Optional 객체
     */
    Optional<Chatroom> findChatroomByMemberIds(Long memberId1, Long memberId2);

    /**
     * 회원의 입장 상태인 모든 채팅방 조회
     *
     * @param memberId 회원 id
     * @return 채팅방 list
     */
    List<Chatroom> findActiveChatrooms(Long memberId);

}
