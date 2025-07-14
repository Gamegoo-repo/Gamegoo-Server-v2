package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.gamegoo.gamegoo_v2.chat.domain.QChat.chat;
import static com.gamegoo.gamegoo_v2.chat.domain.QMemberChatroom.memberChatroom;

@RequiredArgsConstructor
public class ChatRepositoryCustomImpl implements ChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Chat> findRecentChats(Long chatroomId, Long memberId, int pageSize) {
        // 안읽은 메시지 모두 조회
        List<Chat> unreadChats = queryFactory.selectFrom(chat)
                .where(
                        chat.chatroom.id.eq(chatroomId),
                        createdAtGreaterThanLastViewDateSubQuery(memberId),
                        createdAtGreaterOrEqualThanLastJoinDateSubQuery(memberId),
                        isMemberMessageOrMySystemMessage(memberId)
                )
                .orderBy(chat.createdAt.desc())
                .fetch();

        if (unreadChats.size() >= pageSize) { // 안읽은 메시지 개수가 pageSize 이상인 경우, 안읽은 메시지만 모두 리턴
            Chat oldestUnreadChat = unreadChats.get(unreadChats.size() - 1);
            boolean hasNext = hasNextChat(oldestUnreadChat, memberId);

            // createdAt 오름차순으로 정렬
            Collections.reverse(unreadChats);

            return new SliceImpl<>(unreadChats, Pageable.unpaged(), hasNext);
        } else { // 안읽은 메시지가 20개 미만인 경우, 최근 메시지 20개를 조회해 리턴
            List<Chat> chats = queryFactory.selectFrom(chat)
                    .where(
                            chat.chatroom.id.eq(chatroomId),
                            createdAtGreaterOrEqualThanLastJoinDateSubQuery(memberId),
                            isMemberMessageOrMySystemMessage(memberId)
                    )
                    .orderBy(chat.createdAt.desc())
                    .limit(pageSize + 1) // 다음 페이지가 있는지 확인하기 위해 +1
                    .fetch();

            boolean hasNext = chats.size() > pageSize;
            if (hasNext) {
                chats.remove(chats.size() - 1);
            }

            // createdAt 오름차순으로 정렬
            Collections.reverse(chats);

            return new SliceImpl<>(chats, Pageable.unpaged(), hasNext);
        }
    }

    @Override
    public Slice<Chat> findChatsByCursor(Long cursor, Long chatroomId, Long memberId, int pageSize) {
        List<Chat> chats = queryFactory.selectFrom(chat)
                .where(
                        chat.chatroom.id.eq(chatroomId),
                        createdBefore(cursor),
                        createdAtGreaterOrEqualThanLastJoinDateSubQuery(memberId),
                        isMemberMessageOrMySystemMessage(memberId)
                )
                .orderBy(chat.createdAt.desc())
                .limit(pageSize + 1) // 다음 페이지가 있는지 확인하기 위해 +1
                .fetch();

        boolean hasNext = chats.size() > pageSize;
        if (hasNext) {
            chats.remove(chats.size() - 1); // 다음 페이지가 있으면 마지막 요소를 제거
        }

        // createdAt 오름차순으로 정렬
        Collections.reverse(chats);

        return new SliceImpl<>(chats, Pageable.unpaged(), hasNext);
    }

    @Override
    public int countUnreadChats(Long chatroomId, Long memberId) {
        Long result = queryFactory.select(chat.count())
                .from(chat)
                .join(memberChatroom).on(
                        memberChatroom.chatroom.id.eq(chatroomId),
                        memberChatroom.member.id.eq(memberId)
                )
                .where(
                        chat.chatroom.id.eq(chatroomId),
                        memberChatroom.member.id.eq(memberId),
                        createdAtGreaterThanLastViewDateSubQuery(memberId),
                        createdAtGreaterOrEqualThanLastJoinDateSubQuery(memberId),
                        isMemberMessageOrMySystemMessage(memberId)
                )
                .fetchOne();

        return result != null ? result.intValue() : 0;
    }

    /**
     * cursorChat 보다 예전 메시지가 있는지 여부를 반환
     *
     * @param cursorChat
     * @param memberId
     * @return
     */
    private boolean hasNextChat(Chat cursorChat, Long memberId) {
        Chat fetch = queryFactory.selectFrom(chat)
                .where(
                        chat.chatroom.id.eq(cursorChat.getChatroom().getId()),
                        createdBefore(cursorChat.getTimestamp()),
                        createdAtGreaterOrEqualThanLastJoinDateSubQuery(memberId),
                        isMemberMessageOrMySystemMessage(memberId)
                )
                .orderBy(chat.createdAt.desc())
                .limit(1)
                .fetchOne();

        return fetch != null;
    }

    //--- BooleanExpression ---//

    /**
     * lastViewDate 이후에 생성된 메시지인 경우에만 true를 반환
     *
     * @param memberId
     * @return
     */
    private BooleanExpression createdAtGreaterThanLastViewDateSubQuery(Long memberId) {
        return chat.createdAt.gt(
                JPAExpressions
                        .select(memberChatroom.lastViewDate.coalesce(LocalDateTime.MIN))
                        .from(memberChatroom)
                        .where(
                                memberChatroom.chatroom.id.eq(chat.chatroom.id),
                                memberChatroom.member.id.eq(memberId)
                        )
        );
    }

    /**
     * lastJoinDate 이후에 생성된 메시지인 경우에만 true를 반환
     *
     * @param memberId
     * @return
     */
    private BooleanExpression createdAtGreaterOrEqualThanLastJoinDateSubQuery(Long memberId) {
        return chat.createdAt.goe(
                JPAExpressions
                        .select(memberChatroom.lastJoinDate)
                        .from(memberChatroom)
                        .where(
                                memberChatroom.chatroom.id.eq(chat.chatroom.id),
                                memberChatroom.member.id.eq(memberId)
                        )
        );
    }

    /**
     * 회원이 보낸 메시지 또는 내가 받은 시스템 메시지인 경우에만 true를 반환
     *
     * @param memberId
     * @return
     */
    private BooleanExpression isMemberMessageOrMySystemMessage(Long memberId) {
        return chat.toMember.isNull().or(chat.toMember.id.eq(memberId));
    }

    /**
     * 메시지가 cursorTimestamp 이후에 생성된 경우에만 true를 반환
     *
     * @param cursorTimestamp
     * @return
     */
    private BooleanExpression createdBefore(Long cursorTimestamp) {
        return cursorTimestamp != null ? chat.timestamp.lt(cursorTimestamp) : null;
    }

}
