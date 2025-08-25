package com.gamegoo.gamegoo_v2.social.friend.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.external.socket.SocketService;
import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequest;
import com.gamegoo.gamegoo_v2.social.friend.dto.DeleteFriendResponse;
import com.gamegoo.gamegoo_v2.social.friend.dto.FriendInfoResponse;
import com.gamegoo.gamegoo_v2.social.friend.dto.FriendListResponse;
import com.gamegoo.gamegoo_v2.social.friend.dto.FriendRequestResponse;
import com.gamegoo.gamegoo_v2.social.friend.dto.StarFriendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendFacadeService {

    private final FriendService friendService;
    private final MemberService memberService;
    private final SocketService socketService;

    /**
     * 친구 요청 전송 Facade 메소드
     *
     * @param member         회원
     * @param targetMemberId 상대 회원 id
     * @return FriendRequestResponse
     */
    @Transactional
    public FriendRequestResponse sendFriendRequest(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMemberById(targetMemberId);
        FriendRequest friendRequest = friendService.sendFriendRequest(member, targetMember);

        return FriendRequestResponse.of(friendRequest.getToMember().getId(), "친구 요청 전송 성공");
    }

    /**
     * 친구 요청 수락 Facade 메소드
     *
     * @param member         회원
     * @param targetMemberId 상대 회원 id
     * @return FriendRequestResponse
     */
    @Transactional
    public FriendRequestResponse acceptFriendRequest(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMemberById(targetMemberId);
        FriendRequest friendRequest = friendService.acceptFriendRequest(member, targetMember);

        // 친구 요청 수락으로 인한 서로의 온라인 여부 표시를 위해 socket API 호출
        socketService.emitFriendOnlineEvent(member.getId(), targetMemberId);

        return FriendRequestResponse.of(friendRequest.getFromMember().getId(), "친구 요청 수락 성공");
    }

    /**
     * 친구 요청 거절 Facade 메소드
     *
     * @param member         회원
     * @param targetMemberId 상대 회원 id
     * @return FriendRequestResponse
     */
    @Transactional
    public FriendRequestResponse rejectFriendRequest(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMemberById(targetMemberId);
        FriendRequest friendRequest = friendService.rejectFriendRequest(member, targetMember);

        return FriendRequestResponse.of(friendRequest.getFromMember().getId(), "친구 요청 거절 성공");
    }

    /**
     * 친구 요청 취소 Facade 메소드
     *
     * @param member         회원
     * @param targetMemberId 상대 회원 id
     * @return FriendRequestResponse
     */
    @Transactional
    public FriendRequestResponse cancelFriendRequest(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMemberById(targetMemberId);
        FriendRequest friendRequest = friendService.cancelFriendRequest(member, targetMember);

        return FriendRequestResponse.of(friendRequest.getToMember().getId(), "친구 요청 취소 성공");
    }

    /**
     * 친구 즐겨찾기 설정/해제 Facade 메소드
     *
     * @param member         회원
     * @param friendMemberId 친구 회원 id
     * @return StarFriendResponse
     */
    @Transactional
    public StarFriendResponse reverseFriendLiked(Member member, Long friendMemberId) {
        Member friendMember = memberService.findMemberById(friendMemberId);
        Friend friend = friendService.reverseFriendLiked(member, friendMember);

        return StarFriendResponse.of(friend);
    }

    /**
     * 친구 삭제 Facade 메소드
     *
     * @param member         회원
     * @param targetMemberId 상대 회원 id
     * @return DeleteFriendResponse
     */
    @Transactional
    public DeleteFriendResponse deleteFriend(Member member, Long targetMemberId) {
        Member targetMember = memberService.findMemberById(targetMemberId);
        friendService.deleteFriend(member, targetMember);

        return DeleteFriendResponse.of(targetMemberId);
    }

    /**
     * 모든 친구 id 목록 조회 Facade 메소드
     *
     * @param memberId 회원 id
     * @return 친구 회원의 id list
     */
    public List<Long> getFriendIdList(Long memberId) {
        Member member = memberService.findMemberById(memberId);

        return friendService.getFriendIdList(member);
    }

    /**
     * 친구 목록 조회 Facade 메소드
     *
     * @param member 회원
     * @return FriendListResponse
     */
    public FriendListResponse getFriends(Member member) {
        return FriendListResponse.of(friendService.getFriendList(member));
    }

    /**
     * 소환사명으로 친구 조회 Facade 메소드
     *
     * @param member 회원
     * @param query  검색어
     * @return FriendListResponse list
     */
    public List<FriendInfoResponse> searchFriend(Member member, String query) {
        return friendService.searchFriendByGamename(member, query).stream()
                .map(FriendInfoResponse::of)
                .toList();
    }

}
