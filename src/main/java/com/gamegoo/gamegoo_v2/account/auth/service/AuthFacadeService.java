package com.gamegoo.gamegoo_v2.account.auth.service;

import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.RefreshTokenRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.response.RefreshTokenResponse;
import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.chat.service.ChatCommandService;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
