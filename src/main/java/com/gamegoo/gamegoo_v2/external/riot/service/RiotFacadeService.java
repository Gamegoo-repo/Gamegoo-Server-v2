package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.auth.service.AuthService;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.BanService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberChampionService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.account.member.service.AsyncChampionStatsService;
import com.gamegoo.gamegoo_v2.core.common.validator.MemberValidator;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.gamegoo.gamegoo_v2.core.exception.AuthException;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.domain.RSOState;
import com.gamegoo.gamegoo_v2.external.riot.dto.TierDetails;
import com.gamegoo.gamegoo_v2.external.riot.dto.request.RiotJoinRequest;
import com.gamegoo.gamegoo_v2.external.riot.dto.request.RiotVerifyExistUserRequest;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAccountIdResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAuthTokenResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotPuuidGameNameResponse;
import com.gamegoo.gamegoo_v2.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode.INACTIVE_MEMBER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiotFacadeService {

    private final RiotAuthService riotAccountService;
    private final RiotOAuthService riotOAuthService;
    private final RiotInfoService riotInfoService;
    private final RiotRecordService riotRecordService;
    private final MemberService memberService;
    private final MemberChampionService memberChampionService;
    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final OAuthRedirectBuilder oAuthRedirectBuilder;
    private final BanService banService;
    private final MemberValidator memberValidator;
    private final AsyncChampionStatsService asyncChampionStatsService;

    /**
     * 사용가능한 riot 계정인지 검증
     *
     * @param request 소환사명, 태그
     */
    public String verifyRiotAccount(RiotVerifyExistUserRequest request) {
        // puuid 발급 가능한지 검증
        riotAccountService.getPuuid(request.getGameName(), request.getTag());

        return "해당 Riot 계정은 존재합니다";
    }

    @Transactional
    public Member join(RiotJoinRequest request) {
        // [Member] puuid 중복 확인
        memberService.checkDuplicateMemberByPuuid(request.getPuuid());

        // [Riot] gameName, Tag 얻기
        RiotPuuidGameNameResponse response = riotAccountService.getAccountByPuuid(request.getPuuid());

        // [Riot] tier, rank, winrate 얻기
        List<TierDetails> tierWinrateRank = riotInfoService.getTierWinrateRank(request.getPuuid());

        // [Member] member DB에 저장
        Member member = memberService.createMemberRiot(request, response.getGameName(), response.getTagLine(),
                tierWinrateRank);

        // [Riot] 최근 사용한 챔피언 3개 가져오기
        List<ChampionStats> preferChampionStats = riotRecordService.getPreferChampionfromMatch(response.getGameName()
                , response.getPuuid());

        // [Member] Member Champion DB 에서 매핑하기
        memberChampionService.saveMemberChampions(member, preferChampionStats);

        // [Async] 트랜잭션 커밋 후 비동기로 champion stats refresh 실행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    asyncChampionStatsService.refreshChampionStatsAsync(member.getId());
                } catch (Exception e) {
                    // 비동기 작업 실패가 메인 플로우에 영향을 주지 않도록 로그만 기록
                    // 로그 출력 생략 (주요 플로우 방해 안 하기 위해)
                }
            }
        });

        return member;
    }

    /**
     * RSO 콜백 처리 로직
     *
     * @param code  토큰 발급용 코드
     * @param state 프론트 리다이렉트용 state
     * @return 리다리렉트 주소
     */
    @Transactional
    public String processOAuthCallback(String code, String state) {
        // 토큰 교환
        RiotAuthTokenResponse riotAuthTokenResponse = riotOAuthService.exchangeCodeForTokens(code);

        // id_token 파싱 → Riot 사용자 정보 추출
        RiotAccountIdResponse summonerInfo = riotOAuthService.getSummonerInfo(riotAuthTokenResponse.getAccessToken());

        // 리다이렉트 URL 결정
        RSOState decodedRSOState = StateUtil.decodeRSOState(state);
        String targetUrl = decodedRSOState.getRedirect();

        // 만약 사용자 정보가 null 일 경우 롤과 연동되지 않은 사용자
        if (summonerInfo == null) {
            return String.format("%s?error=signup_disabled", targetUrl);
        }

        // DB에서 사용자 존재 여부 확인
        List<Member> memberList = memberService.findMemberByPuuid(summonerInfo.getPuuid());

        // 사용자가 아예 없을 경우, 회원가입 요청
        if (memberList.isEmpty()) {
            return oAuthRedirectBuilder.buildJoinRedirectUrl(targetUrl, state, summonerInfo.getPuuid());
        }

        // 사용자가 있을 경우
        Member member = memberList.get(0);

        // 탈퇴한 사용자인지 확인하기
        memberValidator.throwIfBlind(member, AuthException.class, INACTIVE_MEMBER);

        // 제재 만료 확인 (만료된 제재 자동 해제)
        banService.checkBanExpiry(member);

        // 로그인 진행
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole());

        // refresh token DB에 저장
        authService.updateRefreshToken(member, refreshToken);

        return oAuthRedirectBuilder.buildLoginRedirectUrl(member, state, targetUrl, accessToken, refreshToken);
    }

}
