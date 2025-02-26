package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberChatroomRepository extends JpaRepository<MemberChatroom, Long>, MemberChatroomRepositoryCustom {

    Optional<MemberChatroom> findByMemberIdAndChatroomId(Long memberId, Long chatroomId);

    @Query("""
            SELECT mc.member
            FROM MemberChatroom mc
            WHERE mc.chatroom.id = :chatroomId
            AND mc.member.id != :memberId
            """)
    Optional<Member> findTargetMemberByChatroomIdAndMemberId(@Param("chatroomId") Long chatroomId,
                                                             @Param("memberId") Long memberId);

    @Query("""
            SELECT mc
            FROM MemberChatroom mc
            JOIN FETCH mc.chatroom c
            WHERE mc.member.id = :memberId
            AND mc.lastJoinDate is not null
            ORDER BY COALESCE(c.lastChatAt, mc.lastJoinDate) DESC
            """)
    List<MemberChatroom> findAllActiveMemberChatroomByMemberId(@Param("memberId") Long memberId);

}
