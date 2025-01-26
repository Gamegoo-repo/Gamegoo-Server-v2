package com.gamegoo.gamegoo_v2.repository.friend;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FriendRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private FriendRepository friendRepository;

    private static final int PAGE_SIZE = 10;

    @Nested
    @DisplayName("친구 목록 조회")
    class FindAllFriendsOrderedTest {

        @DisplayName("친구 목록 조회: 친구가 0명인 경우 빈 list를 반환해야 한다.")
        @Test
        void findAllFriendsOrderedNoResult() {
            // when
            List<Friend> friendList = friendRepository.findAllFriendsOrdered(member.getId());

            // then
            assertThat(friendList).isEmpty();
        }

        @DisplayName("친구 목록 조회: 친구가 있는 경우 list를 반환해야 한다.")
        @Test
        void findAllFriendsOrdered() {
            // given
            for (int i = 1; i <= 10; i++) {
                Member toMember = createMember("member" + i + "@gmail.com", "member" + i);
                createFriend(member, toMember);
            }

            // when
            List<Friend> friendList = friendRepository.findAllFriendsOrdered(member.getId());

            // then
            assertThat(friendList).hasSize(10);
            assertThat(friendList.get(0).getToMember().getGameName()).isEqualTo("member1");
            assertThat(friendList.get(9).getToMember().getGameName()).isEqualTo("member9");
        }

        @DisplayName("친구 목록 조회: 조회 결과는 친구 회원의 소환사명에 대해 한>영>숫자 순으로 정렬되어야 한다.")
        @Test
        void findAllFriendsOrderedByGameName() {
            // given
            List<String> gameNameList = Arrays.asList("가", "가1", "가2", "가10", "가a", "가가", "a", "가a1", "가aa", "123");
            for (int i = 0; i < gameNameList.size(); i++) {
                Member toMember = createMember("member" + (i + 1) + "@gmail.com", gameNameList.get(i));
                createFriend(member, toMember);
            }

            // when
            List<Friend> friendList = friendRepository.findAllFriendsOrdered(member.getId());

            // then
            assertThat(friendList).hasSize(10);
            List<String> orderedGameName = Arrays.asList("가", "가가", "가a", "가aa", "가a1", "가1", "가10", "가2", "a", "123");
            for (int i = 0; i < orderedGameName.size(); i++) {
                assertThat(friendList.get(i).getToMember().getGameName()).isEqualTo(
                        orderedGameName.get(i));
            }
        }

    }

    @Nested
    @DisplayName("소환사명으로 친구 검색")
    class FindFriendsByQueryStringTest {

        @DisplayName("소환사명으로 친구 검색: 검색 결과가 없는 경우 빈 리스트를 반환해야 한다.")
        @Test
        void findFriendsByQueryStringSucceedsNoResult() {
            // given
            String query = "targetMember";

            // when
            List<Friend> friendList = friendRepository.findFriendsByQueryString(member.getId(), query);

            // then
            assertThat(friendList).isEmpty();
        }

        @DisplayName("소환사명으로 친구 검색 성공: 검색한 결과가 있는 경우 결과 리스트를 반환해야 한다.")
        @Test
        void findFriendsByQueryStringSucceeds() {
            // given
            Member targetMember1 = createMember("targetMember1@gmail.com", "targetMember");
            Member targetMember2 = createMember("targetMember2@gmail.com", "target");
            Member targetMember3 = createMember("targetMember@gmail.com", "target3");
            Member targetMember4 = createMember("targetMember@gmail.com", "t");
            Member targetMember5 = createMember("targetMember@gmail.com", "TARGET");

            createFriend(member, targetMember1);
            createFriend(member, targetMember2);
            createFriend(member, targetMember3);
            createFriend(member, targetMember4);
            createFriend(member, targetMember5);

            String query = "target";

            // when
            List<Friend> friendList = friendRepository.findFriendsByQueryString(member.getId(), query);

            // then
            assertThat(friendList).hasSize(3);
        }

    }

    @DisplayName("친구 여부 배치 조회")
    @Test
    void isFriendBatch() {
        // given
        List<Long> targetMemberIds = new ArrayList<>();

        // 친구 회원 9명 생성
        List<Member> friendMembers = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            Member toMember = createMember("member" + i + "@gmail.com", "member" + i);
            createFriend(member, toMember);
            friendMembers.add(toMember);
            targetMemberIds.add(toMember.getId());
        }

        // 친구가 아닌 회원 1명 생성
        Member notFriend = createMember("member10@gmail.com", "member10");
        targetMemberIds.add(notFriend.getId());

        // when
        Map<Long, Boolean> friendMap = friendRepository.isFriendBatch(member.getId(), targetMemberIds);

        // then
        assertThat(friendMap).hasSize(targetMemberIds.size());
        for (Member friend : friendMembers) {
            assertThat(friendMap.get(friend.getId())).isTrue();
        }
        assertThat(friendMap.get(notFriend.getId())).isFalse();
    }

    private Friend createFriend(Member fromMember, Member toMember) {
        friendRepository.save(Friend.create(toMember, fromMember));
        return friendRepository.save(Friend.create(fromMember, toMember));
    }

}
