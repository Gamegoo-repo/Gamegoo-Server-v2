package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberGameStyle;
import com.gamegoo.gamegoo_v2.account.member.dto.request.GameStyleRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.IsMikeRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.PositionRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.ProfileImageRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.response.MyProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.dto.response.OtherProfileResponse;
import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacadeService {

    private final MemberService memberService;
    private final FriendService friendService;
    private final BlockService blockService;

    /**
     * 내 프로필 조회
     *
     * @param member    조회할 회원
     * @return          조회된 결과 DTO
     */
    public MyProfileResponse getMyProfile(Member member) {

        // TODO: mannerRank 로직 추가
        double mannerRank = 1.0;

        return MyProfileResponse.of(member, mannerRank);
    }

    /**
     * 다른 사람 프로필 조회
     *
     * @param member    조회할 회원
     * @return          조회된 결과 DTO
     */
    public OtherProfileResponse getOtherProfile(Member member, Long targetMemberId) {

        // memberId로 targetMember 얻기
        Member targetMember = memberService.findMemberById(targetMemberId);

        // TODO: mannerRank, mannerRatingCount 로직 추가
        double mannerRank = 1.0;
        long mannerRatingCount = 1L;

        // 친구 정보 얻기
        boolean isFriend = friendService.isFriend(member, targetMember);
        Long friendRequestMemberId = friendService.getFriendRequestMemberId(member, targetMember);

        // 차단된 사용자인지 확인
        boolean isBlocked = blockService.isBlocked(member, targetMember);

        return OtherProfileResponse.of(targetMember, mannerRank, mannerRatingCount, isFriend, friendRequestMemberId,
                isBlocked);
    }

    /**
     * 프로필 이미지 수정
     *
     * @param member    회원
     * @param request   프로필이미지
     * @return          성공 메세지
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
    public String setIsMike(Member member, IsMikeRequest request) {
        memberService.setIsMike(member, request.getIsMike());
        return "마이크 여부 수정이 완료됐습니다";
    }

    /**
     * 포지션 수정
     *
     * @param member    회원
     * @param request   주/부/원하는 포지션
     * @return          성공 메세지
     */
    @Transactional
    public String setPosition(Member member, PositionRequest request) {
        memberService.setPosition(member, request.getMainP(), request.getSubP(), request.getWantP());
        return "포지션 여부 수정이 완료됐습니다";
    }

    /**
     * 게임 스타일 수정
     * @param member    사용자
     * @param request   게임스타일 리스트
     * @return          성공 메세지
     */
    @Transactional
    public String setGameStyle(Member member, GameStyleRequest request) {
        // request의 Gamestyle 조회
        List<GameStyle> requestGameStyleList = memberService.findRequestGameStyle(request);

        // 현재 DB의 GameStyle 조회
        List<MemberGameStyle> currentMemberGameStyleList = memberService.findCurrentMemberGameStyleList(member);

        // request에 없고, DB에 있는 GameStyle 삭제
        memberService.removeUnnecessaryGameStyles(member, requestGameStyleList, currentMemberGameStyleList);

        // request에 있고, DB에 없는 GameStyle 추가
        memberService.addNewGameStyles(member, requestGameStyleList, currentMemberGameStyleList);

        return "게임 스타일 수정이 완료되었습니다";
    }

}
