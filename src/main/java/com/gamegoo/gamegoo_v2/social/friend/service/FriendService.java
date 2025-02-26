package com.gamegoo.gamegoo_v2.social.friend.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.validator.BlockValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.FriendValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.MemberValidator;
import com.gamegoo.gamegoo_v2.core.event.AcceptFriendRequestEvent;
import com.gamegoo.gamegoo_v2.core.event.RejectFriendRequestEvent;
import com.gamegoo.gamegoo_v2.core.event.SendFriendRequestEvent;
import com.gamegoo.gamegoo_v2.core.exception.FriendException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequest;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequestStatus;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRepository;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final BlockValidator blockValidator;
    private final MemberValidator memberValidator;
    private final FriendValidator friendValidator;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 친구 요청 생성 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @return FriendRequest
     */
    @Transactional
    public FriendRequest sendFriendRequest(Member member, Member targetMember) {
        // targetMember로 나 자신을 요청한 경우 검증
        validateNotSelf(member, targetMember);

        // 상대방의 탈퇴 여부 검증
        memberValidator.throwIfBlind(targetMember);

        // 두 회원의 차단 여부 검증
        validateBlockStatus(member, targetMember);

        // 두 회원이 이미 친구 관계인 경우 검증
        friendValidator.throwIfFriend(member, targetMember);

        // 두 회원 사이 수락 대기중인 친구 요청 존재 여부 검증
        friendValidator.validateNoPendingRequest(member, targetMember);

        // 친구 요청 엔티티 생성 및 저장
        FriendRequest friendRequest = friendRequestRepository.save(FriendRequest.create(member, targetMember));

        // 친구 요청 알림 생성
        eventPublisher.publishEvent(new SendFriendRequestEvent(member.getId(), targetMember.getId()));

        return friendRequest;
    }

    /**
     * targetMember가 보낸 친구 요청 수락 처리 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @return FriendRequest
     */
    @Transactional
    public FriendRequest acceptFriendRequest(Member member, Member targetMember) {
        // targetMember로 나 자신을 요청한 경우 검증
        validateNotSelf(member, targetMember);

        // 수락 대기 상태인 FriendRequest 엔티티 조회 및 검증
        FriendRequest friendRequest = friendRequestRepository.findByFromMemberAndToMemberAndStatus(targetMember,
                        member, FriendRequestStatus.PENDING)
                .orElseThrow(() -> new FriendException(ErrorCode.PENDING_FRIEND_REQUEST_NOT_EXIST));

        // FriendRequest 엔티티 상태 변경
        friendRequest.updateStatus(FriendRequestStatus.ACCEPTED);

        // friend 엔티티 생성 및 저장
        friendRepository.save(Friend.create(member, targetMember));
        friendRepository.save(Friend.create(targetMember, member));

        // targetMember에게 친구 요청 수락 알림 생성
        eventPublisher.publishEvent(new AcceptFriendRequestEvent(member.getId(), targetMember.getId()));

        return friendRequest;
    }

    /**
     * targetMember가 보낸 친구 요청 거절 처리 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @return FriendRequest
     */
    @Transactional
    public FriendRequest rejectFriendRequest(Member member, Member targetMember) {
        // targetMember로 나 자신을 요청한 경우 검증
        validateNotSelf(member, targetMember);

        // 수락 대기 상태인 FriendRequest 엔티티 조회 및 검증
        FriendRequest friendRequest = friendRequestRepository.findByFromMemberAndToMemberAndStatus(targetMember,
                        member, FriendRequestStatus.PENDING)
                .orElseThrow(() -> new FriendException(ErrorCode.PENDING_FRIEND_REQUEST_NOT_EXIST));

        // FriendRequest 엔티티 상태 변경
        friendRequest.updateStatus(FriendRequestStatus.REJECTED);

        // targetMember에게 친구 요청 거절 알림 생성
        eventPublisher.publishEvent(new RejectFriendRequestEvent(member.getId(), targetMember.getId()));

        return friendRequest;
    }

    /**
     * targetMember에게 보낸 친구 요청 취소 처리 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @return FriendRequest
     */
    @Transactional
    public FriendRequest cancelFriendRequest(Member member, Member targetMember) {
        // targetMember로 나 자신을 요청한 경우 검증
        validateNotSelf(member, targetMember);

        // 수락 대기 상태인 FriendRequest 엔티티 조회 및 검증
        FriendRequest friendRequest = friendRequestRepository.findByFromMemberAndToMemberAndStatus(member,
                        targetMember, FriendRequestStatus.PENDING)
                .orElseThrow(() -> new FriendException(ErrorCode.PENDING_FRIEND_REQUEST_NOT_EXIST));

        // FriendRequest 엔티티 상태 변경
        friendRequest.updateStatus(FriendRequestStatus.CANCELLED);

        return friendRequest;
    }


    /**
     * targetMember를 즐겨찾기 설정 또는 해제 처리 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @return Friend
     */
    @Transactional
    public Friend reverseFriendLiked(Member member, Member targetMember) {
        // targetMember로 나 자신을 요청한 경우 검증
        validateNotSelf(member, targetMember);

        // targetMember의 탈퇴 여부 검증
        memberValidator.throwIfBlind(targetMember);

        // 두 회원이 친구 관계인지 검증
        friendValidator.throwIfNotFriend(member, targetMember);

        // liked 상태 변경
        Friend friend = friendRepository.findByFromMemberAndToMember(member, targetMember).get();
        friend.reverseLiked();

        return friend;
    }

    /**
     * 두 회원 사이 친구 관계 삭제 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     */
    @Transactional
    public void deleteFriend(Member member, Member targetMember) {
        // targetMember로 나 자신을 요청한 경우 검증
        validateNotSelf(member, targetMember);

        // 두 회원이 친구 관계인지 검증
        Optional<Friend> optionalFriend1 = friendRepository.findByFromMemberAndToMember(member, targetMember);
        Optional<Friend> optionalFriend2 = friendRepository.findByFromMemberAndToMember(targetMember, member);

        if (optionalFriend1.isEmpty() && optionalFriend2.isEmpty()) {
            throw new FriendException(ErrorCode.MEMBERS_NOT_FRIEND);
        }

        // 친구 관계 삭제
        optionalFriend1.ifPresent(friendRepository::delete);
        optionalFriend2.ifPresent(friendRepository::delete);
    }

    /**
     * 해당 회원의 친구 목록 list 반환하는 메소드
     *
     * @param member 회원
     * @return 친구 list
     */
    public List<Friend> getFriendList(Member member) {
        return friendRepository.findAllFriendsOrdered(member.getId());
    }

    /**
     * 해당 회원의 모든 친구 id 리스트 반환하는 메소드
     *
     * @param member 회원
     * @return 친구 회원 id list
     */
    public List<Long> getFriendIdList(Member member) {
        return member.getFriendList().stream()
                .map(friend -> friend.getToMember().getId())
                .toList();
    }

    /**
     * 소환사명으로 친구 목록 조회하는 메소드
     *
     * @param member 회원
     * @param query  검색어
     * @return 친구 list
     */
    public List<Friend> searchFriendByGamename(Member member, String query) {
        validateSearchQuery(query);
        return friendRepository.findFriendsByQueryString(member.getId(), query);
    }

    /**
     * fromMember와 toMember가 서로 친구 관계이면, 친구 관계 삭제하는 메소드
     *
     * @param fromMember 회원
     * @param toMember   상대 회원
     */
    @Transactional
    public void removeFriendshipIfPresent(Member fromMember, Member toMember) {
        Optional<Friend> optionalFriend = friendRepository.findByFromMemberAndToMember(fromMember, toMember);
        if (optionalFriend.isPresent()) {
            Friend friend = optionalFriend.get();
            friendRepository.deleteById(friend.getId());
        }

        Optional<Friend> reverseFriend = friendRepository.findByFromMemberAndToMember(toMember, fromMember);
        if (reverseFriend.isPresent()) {
            Friend friend = reverseFriend.get();
            friendRepository.deleteById(friend.getId());
        }
    }

    /**
     * fromMember가 toMember에게 보낸 PENDING 상태인 친구 요청을 취소 처리하는 메소드
     *
     * @param fromMember 회원
     * @param toMember   상대 회원
     */
    @Transactional
    public void cancelPendingFriendRequest(Member fromMember, Member toMember) {
        friendRequestRepository.findByFromMemberAndToMemberAndStatus(fromMember, toMember, FriendRequestStatus.PENDING)
                .ifPresent(friendRequest -> friendRequest.updateStatus(FriendRequestStatus.CANCELLED));
    }

    /**
     * 두 회원이 서로 친구인지 여부를 반환하는 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @return 친구 여부
     */
    public boolean isFriend(Member member, Member targetMember) {
        return friendRepository.isFriend(member.getId(), targetMember.getId());
    }

    /**
     * 모든 상대 회원에 대해 서로 친구인지 여부를 반환하는 메소드
     *
     * @param member          회원
     * @param targetMemberIds 상대 회원 id list
     * @return Map<상대 회원 id, 친구 여부>
     */
    public Map<Long, Boolean> isFriendBatch(Member member, List<Long> targetMemberIds) {
        return friendRepository.isFriendBatch(member.getId(), targetMemberIds);
    }

    /**
     * 두 회원 사이 친구 요청이 존재하는 경우 친구 요청을 보낸 회원의 id를 반환하는 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     * @return 친구 요청을 보낸 회원의 id
     */
    public Long getFriendRequestMemberId(Member member, Member targetMember) {
        return friendRequestRepository
                .findBetweenTargetMemberAndStatus(member, targetMember, FriendRequestStatus.PENDING)
                .map(friendRequest -> friendRequest.getFromMember().getId())
                .orElse(null);
    }

    /**
     * 모든 targetMember에 대해 두 회원 사이 친구 요청 보낸 회원의 id 반환하는 메소드
     *
     * @param member          회원
     * @param targetMemberIds 상대 회원 id list
     * @return Map<상대 회원 id, 친구 요청을 보낸 회원의 id>
     */
    public Map<Long, Long> getFriendRequestMemberIdBatch(Member member, List<Long> targetMemberIds) {
        List<FriendRequest> foundRequests = friendRequestRepository.findAllBetweenTargetMembersAndStatus(member,
                targetMemberIds, FriendRequestStatus.PENDING);

        Map<Long, Long> resultMap = new HashMap<>();

        for (FriendRequest fr : foundRequests) {
            Member from = fr.getFromMember();
            Member to = fr.getToMember();

            if (from.getId().equals(member.getId())) { // 내가 요청을 보낸 경우
                Long targetId = to.getId();
                resultMap.put(targetId, member.getId());
            } else { // 상대가 요청을 보낸 경우
                Long targetId = from.getId();
                resultMap.put(targetId, targetId);
            }
        }

        for (Long targetId : targetMemberIds) {
            resultMap.putIfAbsent(targetId, null);
        }

        return resultMap;
    }

    /**
     * 두 회원이 동일하면 에러 발생 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     */
    private void validateNotSelf(Member member, Member targetMember) {
        if (member.getId().equals(targetMember.getId())) {
            throw new FriendException(ErrorCode.FRIEND_BAD_REQUEST);
        }
    }

    /**
     * 두 회원이 서로 차단한 상태이면 에러 발생 메소드
     *
     * @param member       회원
     * @param targetMember 상대 회원
     */
    private void validateBlockStatus(Member member, Member targetMember) {
        blockValidator.throwIfBlocked(member, targetMember, FriendException.class,
                ErrorCode.FRIEND_TARGET_IS_BLOCKED);
        blockValidator.throwIfBlocked(targetMember, member, FriendException.class,
                ErrorCode.BLOCKED_BY_FRIEND_TARGET);
    }

    /**
     * 검색어 길이가 100자 초과이면 에러 발생 메소드
     *
     * @param query 검색어
     */
    private void validateSearchQuery(String query) {
        if (query.length() > 100) {
            throw new FriendException(ErrorCode.FRIEND_SEARCH_QUERY_BAD_REQUEST);
        }
    }

}
