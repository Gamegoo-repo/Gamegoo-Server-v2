package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.dto.request.GameStyleRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.IsMikeRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.PositionRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.ProfileImageRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.response.MyProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.dto.response.OtherProfileResponse;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacadeService {

    private final MemberService memberService;
    private final FriendService friendService;
    private final BlockService blockService;
    private final MemberGameStyleService memberGameStyleService;

    /**
     * 내 프로필 조회
     *
     * @param member 조회할 회원
     * @return 조회된 결과 DTO
     */
    public MyProfileResponse getMyProfile(Member member) {
        return MyProfileResponse.of(member);
    }

    /**
     * 다른 사람 프로필 조회
     *
     * @param member 조회할 회원
     * @return 조회된 결과 DTO
     */
    public OtherProfileResponse getOtherProfile(Member member, Long targetMemberId) {

        // memberId로 targetMember 얻기
        Member targetMember = memberService.findMemberById(targetMemberId);

        // 친구 정보 얻기
        boolean isFriend = friendService.isFriend(member, targetMember);
        Long friendRequestMemberId = friendService.getFriendRequestMemberId(member, targetMember);

        // 차단된 사용자인지 확인
        boolean isBlocked = blockService.isBlocked(member, targetMember);

        return OtherProfileResponse.of(targetMember, isFriend, friendRequestMemberId,
                isBlocked);
    }

    /**
     * 프로필 이미지 수정
     *
     * @param member  회원
     * @param request 프로필이미지
     * @return 성공 메세지
     */
    @Transactional
    public String setProfileImage(Member member, ProfileImageRequest request) {
        memberService.setProfileImage(member, request.getProfileImage());
        return "프로필 이미지 수정이 완료됐습니다";
    }

    /**
     * 마이크 여부 수정
     *
     * @param member  회원
     * @param request 마이크 여부
     * @return 성공 메세지
     */
    @Transactional
    public String setMike(Member member, IsMikeRequest request) {
        memberService.setIsMike(member, request.getMike());
        return "마이크 여부 수정이 완료됐습니다";
    }

    /**
     * 포지션 수정
     *
     * @param member  회원
     * @param request 주/부/원하는 포지션
     * @return 성공 메세지
     */
    @Transactional
    public String setPosition(Member member, PositionRequest request) {
        memberService.setPosition(member, request.getMainP(), request.getSubP(), request.getWantP());
        return "포지션 여부 수정이 완료됐습니다";
    }

    /**
     * 게임 스타일 수정
     *
     * @param member  사용자
     * @param request 게임스타일 리스트
     * @return 성공 메세지
     */
    @Transactional
    public String setGameStyle(Member member, GameStyleRequest request) {
        memberGameStyleService.updateGameStyle(member, request.getGameStyleIdList());
        return "게임 스타일 수정이 완료되었습니다";
    }

}
