package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.dto.data.ChatroomSummaryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long>, ChatroomRepositoryCustom {

    Optional<Chatroom> findByUuid(String uuid);

    @Query(value = """
            SELECT
                cr.chatroom_id AS chatroomId,
                cr.uuid AS chatroomUuid,
                COUNT(uc.chat_id) AS unreadCnt,
                c.contents AS lastChat,
                cr.last_chat_at AS lastChatAt,
                c.timestamp AS lastChatTimestamp,
                (
                    SELECT mc2.member_id
                    FROM member_chatroom mc2
                    WHERE mc2.chatroom_id = cr.chatroom_id
                      AND mc2.member_id != mc.member_id
                    LIMIT 1
                ) AS targetMemberId
            FROM chatroom cr
            JOIN member_chatroom mc
              ON cr.chatroom_id = mc.chatroom_id
            LEFT JOIN chat c
              ON cr.last_chat_id = c.chat_id
            LEFT JOIN (
                SELECT c.chat_id, c.chatroom_id
                FROM chat c
                JOIN member_chatroom mcx ON mcx.chatroom_id = c.chatroom_id
                WHERE mcx.member_id = :memberId
                  AND c.created_at >= IFNULL(mcx.last_view_date, mcx.created_at)
                  AND c.created_at >= mcx.last_join_date
                  AND IFNULL(c.to_member_id, :memberId) = :memberId
            ) uc ON uc.chatroom_id = cr.chatroom_id
            WHERE mc.member_id = :memberId
              AND mc.last_join_date IS NOT NULL
            GROUP BY cr.chatroom_id
            ORDER BY IFNULL(MAX(cr.last_chat_at), MAX(mc.last_join_date)) DESC;
            """, nativeQuery = true)
    List<ChatroomSummaryDTO> findChatroomSummariedByMemberId(@Param("memberId") Long memberId);

}
