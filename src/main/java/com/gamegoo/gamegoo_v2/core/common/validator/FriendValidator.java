package com.gamegoo.gamegoo_v2.core.common.validator;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.exception.FriendException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequestStatus;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRepository;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendValidator {

    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;

    /**
     * 두 회원이 서로 친구이면 예외 발생
     *
     * @param member       회원
     * @param targetMember 회원
     */
    public void throwIfFriend(Member member, Member targetMember) {
        if (friendRepository.isFriend(member.getId(), targetMember.getId())) {
            throw new FriendException(ErrorCode.ALREADY_FRIEND);
        }
    }

    /**
     * 두 회원이 서로 친구가 아니면 예외 발생
     *
     * @param member       회원
     * @param targetMember 회원
     */
    public void throwIfNotFriend(Member member, Member targetMember) {
        if (!friendRepository.isFriend(member.getId(), targetMember.getId())) {
            throw new FriendException(ErrorCode.MEMBERS_NOT_FRIEND);
        }
    }

    /**
     * 두 회원 사이에 PENDING 상태인 친구 요청이 존재하면 예외 발생
     *
     * @param member       회원
     * @param targetMember 상대 회원
     */
    public void validateNoPendingRequest(Member member, Member targetMember) {
        boolean myPendingRequestExists = friendRequestRepository.existsByFromMemberAndToMemberAndStatus(
                member, targetMember, FriendRequestStatus.PENDING);

        if (myPendingRequestExists) {
            throw new FriendException(ErrorCode.MY_PENDING_FRIEND_REQUEST_EXIST);
        }

        boolean targetPendingRequestExists = friendRequestRepository.existsByFromMemberAndToMemberAndStatus(
                targetMember, member, FriendRequestStatus.PENDING);

        if (targetPendingRequestExists) {
            throw new FriendException(ErrorCode.TARGET_PENDING_FRIEND_REQUEST_EXIST);
        }
    }

}
