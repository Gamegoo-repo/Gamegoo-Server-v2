package com.gamegoo.gamegoo_v2.service.friend;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequest;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRequestRepository;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class FriendServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    @Autowired
    FriendService friendService;

    @AfterEach
    void tearDown() {
        friendRequestRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("친구 요청 배치 조회")
    @Test
    void getFriendRequestMemberIdBatch() {
        // given
        List<Long> targetMemberIds = new ArrayList<>();
        Member member = createMember("test@gmail.com", "member");

        // member -> targetMember1 친구 요청 생성
        Member targetMember1 = createMember("test@gmail.com", "member1");
        friendRequestRepository.save(FriendRequest.create(member, targetMember1));
        targetMemberIds.add(targetMember1.getId());

        // targetMember2 -> member 친구 요청 생성
        Member targetMember2 = createMember("test2@gmail.com", "member2");
        friendRequestRepository.save(FriendRequest.create(targetMember2, member));
        targetMemberIds.add(targetMember2.getId());

        // targetMember3 생성
        Member targetMember3 = createMember("test3@gmail.com", "member3");
        targetMemberIds.add(targetMember3.getId());

        // when
        Map<Long, Long> friendRequestMemberIdMap = friendService.getFriendRequestMemberIdBatch(member,
                targetMemberIds);

        // then
        assertThat(friendRequestMemberIdMap.get(targetMember1.getId())).isEqualTo(member.getId());
        assertThat(friendRequestMemberIdMap.get(targetMember2.getId())).isEqualTo(targetMember2.getId());
        assertThat(friendRequestMemberIdMap.get(targetMember3.getId())).isNull();
    }


    private Member createMember(String email, String gameName) {
        return memberRepository.save(Member.builder()
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
                .build());
    }

}
