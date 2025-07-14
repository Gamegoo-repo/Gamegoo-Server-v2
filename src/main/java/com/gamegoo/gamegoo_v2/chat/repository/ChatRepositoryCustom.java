package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import org.springframework.data.domain.Slice;

public interface ChatRepositoryCustom {

    /**
     * 최근 메시지 조회
     *
     * @param chatroomId 채팅방 id
     * @param memberId   회원 id
     * @param pageSize   페이지 크기
     * @return 채팅 Slice 객체
     */
    Slice<Chat> findRecentChats(Long chatroomId, Long memberId, int pageSize);

    /**
     * 커서 기반 메시지 내역 조회
     *
     * @param cursor     채팅 timestamp
     * @param chatroomId 채팅방 id
     * @param memberId   회원 id
     * @param pageSize   페이지 크기
     * @return 채팅 Slice 객체
     */
    Slice<Chat> findChatsByCursor(Long cursor, Long chatroomId, Long memberId, int pageSize);

    /**
     * 해당 채팅방의 안읽은 메시지 개수 조회
     *
     * @param chatroomId 채팅방 id
     * @param memberId   회원 id
     * @return 안읽은 메시지 개수
     */
    int countUnreadChats(Long chatroomId, Long memberId);

}
