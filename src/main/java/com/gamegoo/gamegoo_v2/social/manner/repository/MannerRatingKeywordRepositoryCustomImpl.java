package com.gamegoo.gamegoo_v2.social.manner.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gamegoo.gamegoo_v2.social.manner.domain.QMannerKeyword.mannerKeyword;
import static com.gamegoo.gamegoo_v2.social.manner.domain.QMannerRating.mannerRating;
import static com.gamegoo.gamegoo_v2.social.manner.domain.QMannerRatingKeyword.mannerRatingKeyword;

@RequiredArgsConstructor
public class MannerRatingKeywordRepositoryCustomImpl implements MannerRatingKeywordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Integer> countMannerKeywordByToMemberId(Long memberId) {
        // 매너 키워드 개수 map 초기화
        Map<Long, Integer> resultMap = new HashMap<>();
        for (long i = 1L; i <= 12L; i++) {
            resultMap.put(i, 0);
        }

        List<Tuple> result = queryFactory
                .select(mannerKeyword.id,
                        mannerRatingKeyword.id.count()
                )
                .from(mannerKeyword)
                .leftJoin(mannerRatingKeyword).on(mannerRatingKeyword.mannerKeyword.id.eq(mannerKeyword.id))
                .leftJoin(mannerRating).on(
                        mannerRatingKeyword.mannerRating.id.eq(mannerRating.id)
                                .and(mannerRating.toMember.id.eq(memberId))
                )
                .groupBy(mannerKeyword.id)
                .fetch();

        for (Tuple t : result) {
            Long keywordId = t.get(mannerKeyword.id);
            Long countVal = t.get(mannerRatingKeyword.id.count());
            if (keywordId != null) {
                resultMap.put(keywordId, countVal != null ? countVal.intValue() : 0);
            }
        }

        return resultMap;
    }

}
