package com.gamegoo.gamegoo_v2.social.friend.repository;

import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gamegoo.gamegoo_v2.social.friend.domain.QFriend.friend;

@RequiredArgsConstructor
public class FriendRepositoryCustomImpl implements FriendRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Friend> findAllFriendsOrdered(Long memberId) {
        // 전체 친구 목록 조회
        List<Friend> allFriends = queryFactory.selectFrom(friend)
                .join(friend.toMember).fetchJoin()
                .where(friend.fromMember.id.eq(memberId))
                .fetch();

        // 친구 목록 전체 정렬
        allFriends.sort(
                (f1, f2) -> memberNameComparator.compare(f1.getToMember().getGameName(),
                        f2.getToMember().getGameName()));
        return allFriends;
    }

    @Override
    public List<Friend> findFriendsByQueryString(Long memberId, String queryString) {
        // query string으로 시작하는 소환사명을 갖는 모든 친구 목록 조회
        List<Friend> result = queryFactory.selectFrom(friend)
                .where(friend.fromMember.id.eq(memberId)
                        .and(friend.toMember.gameName.startsWith(queryString))
                )
                .fetch();

        result.sort(
                (f1, f2) -> memberNameComparator.compare(f1.getToMember().getGameName(),
                        f2.getToMember().getGameName()));

        return result;
    }

    @Override
    public boolean isFriend(Long memberId, Long targetMemberId) {
        long count = queryFactory
                .selectFrom(friend)
                .where(
                        (friend.fromMember.id.eq(memberId).and(friend.toMember.id.eq(targetMemberId)))
                                .or(friend.fromMember.id.eq(targetMemberId).and(friend.toMember.id.eq(memberId)))
                )
                .fetch()
                .size();

        return count == 2;
    }

    @Override
    public Map<Long, Boolean> isFriendBatch(Long memberId, List<Long> targetMemberIds) {
        // fromMember, toMember 쌍 전체 조회
        List<Tuple> results = queryFactory
                .select(friend.fromMember.id, friend.toMember.id)
                .from(friend)
                .where(
                        friend.fromMember.id.eq(memberId).and(friend.toMember.id.in(targetMemberIds))
                                .or(friend.fromMember.id.in(targetMemberIds).and(friend.toMember.id.eq(memberId)))
                )
                .fetch();

        // friend 레코드 개수 count
        Map<Long, Integer> friendCountMap = new HashMap<>();

        for (Tuple row : results) {
            Long fromId = row.get(friend.fromMember.id);
            Long toId = row.get(friend.toMember.id);

            if (fromId.equals(memberId)) {
                friendCountMap.merge(toId, 1, Integer::sum);
            } else {
                friendCountMap.merge(fromId, 1, Integer::sum);
            }
        }

        // targetId에 대한 친구 여부 map 생성
        Map<Long, Boolean> result = new HashMap<>();
        for (Long targetId : targetMemberIds) {
            Integer count = friendCountMap.getOrDefault(targetId, 0);
            boolean isFriend = (count == 2);
            result.put(targetId, isFriend);
        }

        return result;
    }

    /**
     * cursorId에 해당하는 Friend 객체의 다음 인덱스 찾기
     *
     * @param allFriends
     * @param cursorId
     * @return
     */
    private static int findCursorIndex(List<Friend> allFriends, Long cursorId) {
        if (cursorId == null) {
            return 0;
        }

        for (int i = 0; i < allFriends.size(); i++) {
            if (allFriends.get(i).getToMember().getId().equals(cursorId)) {
                return i + 1;
            }
        }
        return 0; // cursorId에 해당하는 객체를 찾지 못하면 0을 리턴
    }

    private static final Comparator<String> memberNameComparator = (s1, s2) -> {
        int length1 = s1.length();
        int length2 = s2.length();
        int minLength = Math.min(length1, length2);

        // 각 문자 비교
        for (int i = 0; i < minLength; i++) {
            int result = compareChars(s1.charAt(i), s2.charAt(i));
            if (result != 0) {
                return result;
            }
        }

        // 앞부분이 동일하면, 길이가 짧은 것이 앞으로 오도록 정렬
        return Integer.compare(length1, length2);
    };

    /**
     * 문자 비교 메서드: 한글 -> 영문자 -> 숫자 순으로 우선순위 지정
     *
     * @param c1
     * @param c2
     * @return
     */
    private static int compareChars(char c1, char c2) {
        boolean isC1Korean = isKorean(c1);
        boolean isC2Korean = isKorean(c2);

        // 한글과 영문자/숫자를 구분하여 우선순위 설정
        if (isC1Korean && !isC2Korean) {
            return -1; // 한글은 영문자/숫자보다 먼저
        } else if (!isC1Korean && isC2Korean) {
            return 1; // 영문자/숫자는 한글보다 뒤
        } else if (Character.isDigit(c1) && Character.isDigit(c2)) {
            return Character.compare(c1, c2); // 둘 다 숫자인 경우 숫자 비교
        } else if (Character.isDigit(c1)) {
            return 1; // 숫자는 항상 뒤로
        } else if (Character.isDigit(c2)) {
            return -1; // 숫자는 항상 뒤로
        } else {
            return Character.compare(c1, c2); // 기본적으로 문자 비교 (영문자끼리 등)
        }
    }

    /**
     * 한글 여부를 판별
     *
     * @param c
     * @return
     */
    private static boolean isKorean(char c) {
        return (c >= 0x1100 && c <= 0x11FF) || // 한글 자모
                (c >= 0xAC00 && c <= 0xD7AF) || // 한글 음절
                (c >= 0x3130 && c <= 0x318F);   // 한글 호환 자모
    }

}
