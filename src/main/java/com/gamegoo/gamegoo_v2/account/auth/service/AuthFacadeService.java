package com.gamegoo.gamegoo_v2.account.auth.service;

import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.RefreshTokenRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.response.RefreshTokenResponse;
import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.RejoinRequest;
import com.gamegoo.gamegoo_v2.account.member.service.BanService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.chat.service.ChatCommandService;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotJoinResponse;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthFacadeService {

    private final MemberService memberService;
    private final ChatCommandService chatCommandService;
    private final FriendService friendService;
    private final MannerService mannerService;
    private final BoardService boardService;
    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final BanService banService;

    /**
     * 로그아웃
     *
     * @param member 사용자
     * @return 메세지
     */
    public String logout(Member member) {
        authService.deleteRefreshToken(member);
        return "로그아웃이 완료되었습니다.";
    }

    /**
     * 리프레시 토큰으로 토큰 업데이트
     *
     * @param request 리프레시 토큰
     * @return 사용자 정보
     */
    public RefreshTokenResponse updateToken(RefreshTokenRequest request) {
        // refresh 토큰 검증
        authService.verifyRefreshToken(request.getRefreshToken());

        // memberId 조회
        Long memberId = jwtProvider.getMemberId(request.getRefreshToken());
        Role role = jwtProvider.getRole(request.getRefreshToken());

        // jwt 토큰 재발급
        String accessToken = jwtProvider.createAccessToken(memberId, role);
        String refreshToken = jwtProvider.createRefreshToken(memberId, role);

        // memberId로 member 엔티티 조회
        Member member = memberService.findMemberById(memberId);

        // refreshToken 저장
        authService.updateRefreshToken(member, refreshToken);

        return RefreshTokenResponse.of(memberId, accessToken, refreshToken);
    }

    public String blindMember(Member member) {
        // Member 테이블에서 blind 처리
        memberService.deactivateMember(member);

        // 해당 회원이 속한 모든 채팅방에서 퇴장 처리
        chatCommandService.exitAllChatroom(member);

        // 해당 회원이 보낸 모든 친구 요청 취소 처리
        friendService.cancelAllFriendRequestsByFromMember(member);

        // 해당 회원이 받은 모든 친구 요청 취소 처리
        friendService.cancelAllFriendRequestsByToMember(member);

        // 게시판 글 삭제 처리
        boardService.deleteAllBoardByMember(member);

        // 매너, 비매너 평가 기록 삭제 처리
        mannerService.deleteAllMannerRatingsByMember(member);

        // refresh Token 삭제하기
        authService.deleteRefreshToken(member);

        return "탈퇴처리가 완료되었습니다";
    }

    public String createTestAccessToken(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        return jwtProvider.createAccessToken(member.getId(), member.getRole());
    }

    @Transactional
    public RiotJoinResponse rejoinMember(RejoinRequest request) {
        // 실제 있는 사용자인지 검증
        List<Member> memberByPuuid = memberService.findMemberByPuuid(request.getPuuid());
        if (memberByPuuid.isEmpty()) {
            throw new MemberException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Member member = memberByPuuid.get(0);

        // 제재 있는지 검증
        // 만료된 제재 자동 해제
        banService.checkBanExpiry(member);

        // TODO: 제재가 있을 경우 탈퇴 후 재가입 불가능 (?)
        if (member.isBanned()) {
            throw new MemberException(ErrorCode.MEMBER_BANNED);
        }

        // 탈퇴 해제
        memberService.activateMember(member);

        // 로그인 진행
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole());

        // refresh token DB에 저장
        authService.updateRefreshToken(member, refreshToken);

        return RiotJoinResponse.of(member, accessToken, refreshToken);
    }

}
