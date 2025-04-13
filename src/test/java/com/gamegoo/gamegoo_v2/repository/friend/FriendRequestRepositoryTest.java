package com.gamegoo.gamegoo_v2.repository.friend;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequest;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequestStatus;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FriendRequestRepositoryTest extends RepositoryTestSupport {

    @Autowired
    FriendRequestRepository friendRequestRepository;

    @AfterEach
    void tearDown() {
        friendRequestRepository.deleteAllInBatch();
    }

    @DisplayName("해당 회원이 보낸 친구 요청 상태 배치 업데이트")
    @Test
    void updateAllStatusByFromMember() {
        // given
        for (int i = 1; i <= 10; i++) {
            Member toMember = createMember("member" + i + "@gmail.com", "member" + i);
            createFriendRequest(member, toMember);
        }

        // when
        friendRequestRepository.updateAllStatusByFromMember(member, FriendRequestStatus.CANCELLED);

        // then
        List<FriendRequest> updatedList = friendRequestRepository.findAllByFromMember(member);
        assertThat(updatedList).hasSize(10);
        assertThat(updatedList).allSatisfy(fr -> assertThat(fr.getStatus()).isEqualTo(FriendRequestStatus.CANCELLED));
    }

    @DisplayName("해당 회원이 받은 친구 요청 상태 배치 업데이트")
    @Test
    void updateAllStatusByToMember() {
        // given
        for (int i = 1; i <= 10; i++) {
            Member toMember = createMember("member" + i + "@gmail.com", "member" + i);
            createFriendRequest(toMember, member);
        }

        // when
        friendRequestRepository.updateAllStatusByToMember(member, FriendRequestStatus.CANCELLED);

        // then
        List<FriendRequest> updatedList = friendRequestRepository.findAllByToMember(member);
        assertThat(updatedList).hasSize(10);
        assertThat(updatedList).allSatisfy(fr -> assertThat(fr.getStatus()).isEqualTo(FriendRequestStatus.CANCELLED));
    }

    private FriendRequest createFriendRequest(Member fromMember, Member toMember) {
        return friendRequestRepository.save(FriendRequest.create(fromMember, toMember));
    }

}
