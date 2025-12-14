package com.gamegoo.gamegoo_v2.social.block.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gamegoo.gamegoo_v2.social.block.domain.QBlock.block;

@RequiredArgsConstructor
public class BlockRepositoryCustomImpl implements BlockRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Boolean> isBlockedByTargetMembersBatch(List<Long> targetMemberIds, Long memberId) {
        Set<Long> blockedSet = new HashSet<>(
                queryFactory
                        .select(block.blockerMember.id)
                        .from(block)
                        .where(
                                block.blockerMember.id.in(targetMemberIds),
                                block.blockedMember.id.eq(memberId),
                                block.deleted.eq(false)
                        )
                        .fetch()
        );

        return targetMemberIds.stream()
                .collect(Collectors.toMap(
                        targetId -> targetId,
                        blockedSet::contains
                ));
    }

    @Override
    public Map<Long, Boolean> hasBlockedTargetMembersBatch(List<Long> targetMemberIds, Long memberId){
        Set<Long> blockedSet = new HashSet<>(
                queryFactory
                        .select(block.blockedMember.id)
                        .from(block)
                        .where(
                                block.blockerMember.id.eq(memberId),
                                block.blockedMember.id.in(targetMemberIds),
                                block.deleted.eq(false)
                        )
                        .fetch()
        );
        return targetMemberIds.stream()
                .collect(Collectors.toMap(
                        targetId -> targetId,
                        blockedSet::contains
                ));
    }

}
