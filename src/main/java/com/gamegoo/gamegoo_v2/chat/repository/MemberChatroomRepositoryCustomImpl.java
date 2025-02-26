package com.gamegoo.gamegoo_v2.chat.repository;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gamegoo.gamegoo_v2.chat.domain.QMemberChatroom.memberChatroom;

@RequiredArgsConstructor
public class MemberChatroomRepositoryCustomImpl implements MemberChatroomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Member> findTargetMembersBatch(List<Long> chatroomIds, Long memberId) {
        List<Tuple> results = queryFactory
                .select(memberChatroom.chatroom.id, memberChatroom.member)
                .from(memberChatroom)
                .where(
                        memberChatroom.chatroom.id.in(chatroomIds),
                        memberChatroom.member.id.ne(memberId)
                )
                .fetch();

        Map<Long, Member> resultMap = new HashMap<>();
        for (Tuple elem : results) {
            Long chatroomId = elem.get(memberChatroom.chatroom.id);
            Member targetMember = elem.get(memberChatroom.member);
            resultMap.put(chatroomId, targetMember);
        }

        return resultMap;
    }

}
