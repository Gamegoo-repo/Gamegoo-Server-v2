package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.dto.data.ChatroomSummaryDTO;
import com.gamegoo.gamegoo_v2.chat.dto.data.ChatroomTargetDTO;
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
                MIN(m_other.member_id) AS targetMemberId,
                m_other.game_name AS targetMemberName,
                m_other.profile_image AS targetMemberImg,
                m_other.blind AS blind
            FROM chatroom cr
            JOIN member_chatroom mc
              ON cr.chatroom_id = mc.chatroom_id
            LEFT JOIN member_chatroom mc_other
              ON mc_other.chatroom_id = cr.chatroom_id
             AND mc_other.member_id != mc.member_id
            LEFT JOIN member m_other
              ON m_other.member_id = mc_other.member_id
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
                  AND c.from_member_id != :memberId
            ) uc ON uc.chatroom_id = cr.chatroom_id
            WHERE mc.member_id = :memberId
              AND mc.last_join_date IS NOT NULL
            GROUP BY cr.chatroom_id, m_other.game_name, m_other.profile_image
            ORDER BY IFNULL(MAX(cr.last_chat_at), MAX(mc.last_join_date)) DESC;
            """, nativeQuery = true)
    List<ChatroomSummaryDTO> findChatroomSummariedByMemberId(@Param("memberId") Long memberId);

    @Query(value = """
            select
                m.member_id AS targetMemberId,
                IF(f.from_member_id IS NOT NULL, TRUE, FALSE) AS isFriend,
                IF(b.blocker_id IS NOT NULL, TRUE, FALSE) AS isBlocked,
                fr.from_member_id as friendRequestMemberId
            from member m
            left join friend f
            on m.member_id = f.from_member_id and f.to_member_id = :memberId
            left join block b
            on m.member_id = b.blocker_id and b.blocked_id = :memberId and b.deleted = false
            left join friend_request fr
            on ((m.member_id = fr.from_member_id and fr.to_member_id = :memberId) or (m.member_id = fr.to_member_id and fr.from_member_id = :memberId)) and fr.status ='PENDING'
            where m.member_id in :targetMemberIds
            """, nativeQuery = true)
    List<ChatroomTargetDTO> findChatroomTargetsByMemberId(@Param("memberId") Long memberId,
                                                          @Param("targetMemberIds") List<Long> targetMemberIds);

}
