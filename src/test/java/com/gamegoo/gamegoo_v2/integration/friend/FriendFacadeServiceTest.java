package com.gamegoo.gamegoo_v2.integration.friend;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.exception.FriendException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.block.repository.BlockRepository;
import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import com.gamegoo.gamegoo_v2.social.friend.dto.DeleteFriendResponse;
import com.gamegoo.gamegoo_v2.social.friend.dto.FriendInfoResponse;
import com.gamegoo.gamegoo_v2.social.friend.dto.FriendListResponse;
import com.gamegoo.gamegoo_v2.social.friend.dto.StarFriendResponse;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRepository;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendFacadeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class FriendFacadeServiceTest {

    @Autowired
    FriendFacadeService friendFacadeService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BlockRepository blockRepository;

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    MemberRecentStatsRepository memberRecentStatsRepository;

    private static final String TARGET_EMAIL = "target@naver.com";
    private static final String TARGET_GAMENAME = "target";

    private Member member;

    @BeforeEach
    void setUp() {
        member = createMember("test@gmail.com", "member");
    }

    @AfterEach
    void tearDown() {
        memberRecentStatsRepository.deleteAll();
        friendRepository.deleteAllInBatch();
        blockRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("친구 즐겨찾기 설정/해제")
    class ReverseFriendLikedTest {

        @DisplayName("친구 즐겨찾기 설정 성공: 즐겨찾기 되지 않은 친구를 요청한 경우 즐겨찾기가 설정된다.")
        @Test
        void reverseFriendLikedSucceedsWhenNotLiked() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 두 회원 간 친구 관계 생성
            friendRepository.save(Friend.create(member, targetMember));
            friendRepository.save(Friend.create(targetMember, member));

            // when
            StarFriendResponse response = friendFacadeService.reverseFriendLiked(member, targetMember.getId());

            // then
            assertThat(response.getMessage()).isEqualTo("친구 즐겨찾기 설정 성공");
            assertThat(response.getFriendMemberId()).isEqualTo(targetMember.getId());

            // 즐겨찾기 처리 되었는지 검증
            Friend friend = friendRepository.findByFromMemberAndToMember(member, targetMember).get();
            assertThat(friend.isLiked()).isTrue();
        }

        @DisplayName("친구 즐겨찾기 해제 성공: 즐겨찾기 된 친구를 요청한 경우 즐겨찾기가 해제된다.")
        @Test
        void reverseFriendLikedSucceedsWhenLiked() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 두 회원 간 친구 관계 생성
            Friend savedFriend = friendRepository.save(Friend.create(member, targetMember));
            friendRepository.save(Friend.create(targetMember, member));

            // member -> targetMember 즐겨찾기 설정
            savedFriend.reverseLiked();
            friendRepository.save(savedFriend);

            // when
            StarFriendResponse response = friendFacadeService.reverseFriendLiked(member, targetMember.getId());

            // then
            assertThat(response.getMessage()).isEqualTo("친구 즐겨찾기 해제 성공");
            assertThat(response.getFriendMemberId()).isEqualTo(targetMember.getId());

            // 즐겨찾기 처리 되었는지 검증
            Friend friend = friendRepository.findByFromMemberAndToMember(member, targetMember).get();
            assertThat(friend.isLiked()).isFalse();
        }

        @DisplayName("친구 즐겨찾기 설정/해제 실패: 본인 id를 요청한 경우 예외가 발생한다.")
        @Test
        void reverseFriendLiked_shouldThrownWhenTargetIsSelf() {
            // when // then
            assertThatThrownBy(() -> friendFacadeService.reverseFriendLiked(member, member.getId()))
                    .isInstanceOf(FriendException.class)
                    .hasMessage(ErrorCode.FRIEND_BAD_REQUEST.getMessage());
        }

        @DisplayName("친구 즐겨찾기 설정/해제 실패: 상대가 탈퇴한 회원인 경우 예외가 발생한다.")
        @Test
        void reverseFriendLiked_shouldThrownWhenTargetIsBlind() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 대상 회원을 탈퇴 처리
            targetMember.updateBlind(true);
            memberRepository.save(targetMember);

            // when // then
            assertThatThrownBy(() -> friendFacadeService.reverseFriendLiked(member, targetMember.getId()))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("친구 즐겨찾기 설정/해제 실패: 상대가 친구가 아닌 경우 예외가 발생한다.")
        @Test
        void reverseFriendLiked_shouldThrownWhenTargetIsNotFriend() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // when // then
            assertThatThrownBy(() -> friendFacadeService.reverseFriendLiked(member, targetMember.getId()))
                    .isInstanceOf(FriendException.class)
                    .hasMessage(ErrorCode.MEMBERS_NOT_FRIEND.getMessage());
        }

    }

    @Nested
    @DisplayName("친구 삭제")
    class DeleteFriendTest {

        @DisplayName("친구 삭제 성공")
        @Test
        void deleteFriendSucceeds() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // 두 회원 간 친구 관계 생성
            friendRepository.save(Friend.create(member, targetMember));
            friendRepository.save(Friend.create(targetMember, member));

            // when
            DeleteFriendResponse response = friendFacadeService.deleteFriend(member, targetMember.getId());

            // then
            assertThat(response.getMessage()).isEqualTo("친구 삭제 성공");
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());

            // friend 엔티티 삭제 되었는지 검증
            assertThat(friendRepository.existsByFromMemberAndToMember(member, targetMember)).isFalse();
            assertThat(friendRepository.existsByFromMemberAndToMember(targetMember, member)).isFalse();
        }

        @DisplayName("친구 삭제 실패: 본인 id를 요청한 경우 예외가 발생한다.")
        @Test
        void deleteFriend_shouldThrowWhenTargetIsSelf() {
            // when // then
            assertThatThrownBy(() -> friendFacadeService.deleteFriend(member, member.getId()))
                    .isInstanceOf(FriendException.class)
                    .hasMessage(ErrorCode.FRIEND_BAD_REQUEST.getMessage());
        }

        @DisplayName("친구 삭제 실패: 두 회원이 친구 관계가 아닌 경우 예외가 발생한다.")
        @Test
        void deleteFriend_shouldThrowWhenNotFriend() {
            // given
            Member targetMember = createMember(TARGET_EMAIL, TARGET_GAMENAME);

            // when // then
            assertThatThrownBy(() -> friendFacadeService.deleteFriend(member, targetMember.getId()))
                    .isInstanceOf(FriendException.class)
                    .hasMessage(ErrorCode.MEMBERS_NOT_FRIEND.getMessage());
        }

    }

    @Nested
    @DisplayName("모든 친구 id 조회")
    class GetFriendIdList {

        @DisplayName("모든 친구 id 조회 성공: 친구가 있는 경우")
        @Test
        void getFriendIdListSucceeds() {
            // given
            for (int i = 1; i <= 5; i++) {
                Member targetMember = createMember("member" + i + "@gmail.com", "member" + i);

                // 친구 관계 생성
                friendRepository.save(Friend.create(member, targetMember));
                friendRepository.save(Friend.create(targetMember, member));
            }

            // when
            List<Long> friendIdList = friendFacadeService.getFriendIdList(member.getId());

            // then
            assertThat(friendIdList).hasSize(5);
        }

        @DisplayName("모든 친구 id 조회 성공: 친구가 없는 경우")
        @Test
        void getFriendIdListSucceedsWhenNoFriend() {
            // when
            List<Long> friendIdList = friendFacadeService.getFriendIdList(member.getId());

            // then
            assertThat(friendIdList).hasSize(0);
        }

    }

    @Nested
    @DisplayName("친구 목록 조회")
    class GetFriendList {

        @DisplayName("친구 목록 조회 성공: 친구가 없는 경우")
        @Test
        void getFriendListSucceedsWhenNoFriend() {
            // when
            FriendListResponse friends = friendFacadeService.getFriends(member);

            // then
            assertThat(friends.getFriendInfoList()).isEmpty();
            assertThat(friends.getListSize()).isEqualTo(0);
        }

        @DisplayName("친구 목록 조회 성공: 친구가 있는 경우")
        @Test
        void getFriendListSucceedsNextPage() {
            // given
            List<Member> targetMembers = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                Member targetMember = createMember("member" + i + "@gmail.com", "member" + i);

                // 친구 관계 생성
                friendRepository.save(Friend.create(member, targetMember));
                friendRepository.save(Friend.create(targetMember, member));

                targetMembers.add(targetMember);
            }

            Long cursor = targetMembers.get(3).getId();

            // when
            FriendListResponse friends = friendFacadeService.getFriends(member);

            // then
            assertThat(friends.getFriendInfoList()).hasSize(15);
            assertThat(friends.getListSize()).isEqualTo(15);
        }

    }

    @Nested
    @DisplayName("소환사명으로 친구 검색")
    class searchFriendByGamename {

        @DisplayName("소환사명으로 친구 검색 성공: 친구가 없는 경우")
        @Test
        void searchFriendByGamenameSucceedsNoResult() {
            // given
            String query = "targetMember";

            // when
            List<FriendInfoResponse> friendList = friendFacadeService.searchFriend(member, query);

            // then
            assertThat(friendList).isEmpty();
        }

        @DisplayName("소환사명으로 친구 검색 성공: 친구가 있는 경우")
        @Test
        void searchFriendByGamenameSucceeds() {
            // given
            String query = "target";

            Member targetMember = createMember("targetMember@gmail.com", "targetMember");

            // 친구 관계 생성
            friendRepository.save(Friend.create(member, targetMember));
            friendRepository.save(Friend.create(targetMember, member));

            // when
            List<FriendInfoResponse> friendList = friendFacadeService.searchFriend(member, query);

            // then
            assertThat(friendList).hasSize(1);
        }

        @DisplayName("소환사명으로 친구 검색 실패: query 길이 제한을 초과한 경우 예외가 발생한다.")
        @Test
        void searchFriendByGamename_shouldThrowWhenQueryTooLong() {
            // given
            String query = "a".repeat(101);

            // when // then
            assertThatThrownBy(() -> friendFacadeService.searchFriend(member, query))
                    .isInstanceOf(FriendException.class)
                    .hasMessage(ErrorCode.FRIEND_SEARCH_QUERY_BAD_REQUEST.getMessage());
        }

    }

    private Member createMember(String email, String gameName) {
        Member member = Member.builder()
                .email(email)
                .password("testPassword")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag("TAG")
                .soloTier(Tier.IRON)
                .soloRank(0)
                .soloWinRate(0.0)
                .soloGameCount(0)
                .freeTier(Tier.IRON)
                .freeRank(0)
                .freeWinRate(0.0)
                .freeGameCount(0)
                .isAgree(true)
                .build();

        memberRecentStatsRepository.save(MemberRecentStats.builder()
                .member(member)
                .build());

        return memberRepository.save(member);
    }

}
