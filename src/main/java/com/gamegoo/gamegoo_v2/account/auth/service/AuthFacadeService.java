package com.gamegoo.gamegoo_v2.account.auth.service;

import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.JoinRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.LoginRequestQA;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.RefreshTokenRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.response.LoginResponse;
import com.gamegoo.gamegoo_v2.account.auth.dto.response.RefreshTokenResponse;
import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.AsyncChampionStatsService;
import com.gamegoo.gamegoo_v2.account.member.service.BanService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberChampionService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.chat.service.ChatCommandService;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.TierDetails;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotAuthService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotInfoService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotRecordService;
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
    private final RiotAuthService riotAccountService;
    private final RiotRecordService riotRecordService;
    private final RiotInfoService riotInfoService;
    private final MemberChampionService memberChampionService;
    private final ChatCommandService chatCommandService;
    private final FriendService friendService;
    private final MannerService mannerService;
    private final BoardService boardService;
    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final PasswordService passwordService;
    private final BanService banService;
    private final AsyncChampionStatsService asyncChampionStatsService;

    /**
     * 회원가입
     *
     * @param request 회원가입용 정보
     */
    public String join(JoinRequest request) {
        // [Member] 중복확인
        memberService.checkDuplicateMemberByEmail(request.getEmail());

        // [Riot] 존재하는 소환사인지 검증 & puuid 얻기
        String puuid = riotAccountService.getPuuid(request.getGameName(), request.getTag());

        // [Riot] tier, rank, winrate 얻기
        List<TierDetails> tierWinrateRank = riotInfoService.getTierWinrateRank(puuid);

        // [Member] member DB에 저장
        Member member = memberService.createMemberGeneral(request, tierWinrateRank);

        // [Riot] 최근 사용한 챔피언 3개 가져오기
        List<ChampionStats> preferChampionStats = riotRecordService.getPreferChampionfromMatch(request.getGameName(),
                puuid);

        // [Member] Member Champion DB에서 매핑하기
        memberChampionService.saveMemberChampions(member, preferChampionStats);

        // [Async] 비동기로 champion stats refresh 실행
        asyncChampionStatsService.refreshChampionStatsAsync(member.getId());

        return "회원가입이 완료되었습니다.";
    }

    /**
     * 로그인
     *
     * @param request 이메일,비밀번호
     * @return 사용자 정보
     */
    public LoginResponse login(LoginRequestQA request) {
        // email 검증
        Member member = memberService.findMemberByEmail(request.getEmail());

        // password 검증
        passwordService.verifyRawPassword(member, request.getPassword());

        // 해당 사용자의 정보를 가진 jwt 토큰 발급
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole());

        // DB에 저장
        authService.updateRefreshToken(member, refreshToken);

        // 제재 만료 확인 (만료된 제재 자동 해제)
        banService.checkBanExpiry(member);

        // 제재 메시지 생성
        String banMessage = null;
        if (member.isBanned()) {
            banMessage = banService.getBanReasonMessage(member.getBanType());
        }

        return LoginResponse.of(member, accessToken, refreshToken, banMessage);
    }


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
