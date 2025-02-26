package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.gamegoo.gamegoo_v2.chat.domain.QChatroom.chatroom;
import static com.gamegoo.gamegoo_v2.chat.domain.QMemberChatroom.memberChatroom;

@RequiredArgsConstructor
public class ChatroomRepositoryCustomImpl implements ChatroomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 두 member 사이의 chatroom 엔티티 반환하는 메소드
     *
     * @param memberId1
     * @param memberId2
     * @return
     */
    @Override
    public Optional<Chatroom> findChatroomByMemberIds(Long memberId1, Long memberId2) {
        Chatroom chatroomEntity = queryFactory
                .select(chatroom)
                .from(memberChatroom)
                .join(chatroom).on(memberChatroom.chatroom.id.eq(chatroom.id))
                .where(memberChatroom.member.id.in(memberId1, memberId2))
                .groupBy(memberChatroom.chatroom.id)
                .having(memberChatroom.member.id.count().eq(2L))
                .fetchFirst();

        return Optional.ofNullable(chatroomEntity);
    }

    @Override
    public List<Chatroom> findActiveChatrooms(Long memberId) {
        return queryFactory
                .select(chatroom)
                .from(memberChatroom)
                .join(memberChatroom.chatroom, chatroom)
                .where(
                        memberChatroom.member.id.eq(memberId),
                        memberChatroom.lastJoinDate.isNotNull()
                )
                .fetch();
    }

}
