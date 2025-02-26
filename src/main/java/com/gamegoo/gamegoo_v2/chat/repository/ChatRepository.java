package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long>, ChatRepositoryCustom {

    List<Chat> findByChatroomIdAndFromMemberId(Long chatroomId, Long fromMemberId);

    Optional<Chat> findByChatroomAndTimestamp(Chatroom chatroom, Long timestamp);

}
