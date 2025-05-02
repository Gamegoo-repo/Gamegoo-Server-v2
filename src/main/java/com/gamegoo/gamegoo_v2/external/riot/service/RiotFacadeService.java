package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.auth.service.AuthService;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberChampionService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.TierDetails;
import com.gamegoo.gamegoo_v2.external.riot.dto.request.RiotJoinRequest;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAuthTokenResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAccountIdResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.request.RiotVerifyExistUserRequest;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotPuuidGameNameResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    @Value(value = "${spring.front_url}")
    private String frontUrl;

    /**
     * 사용가능한 riot 계정인지 검증
     *
     * @param request 소환사명, 태그
     */
    public String verifyRiotAccount(RiotVerifyExistUserRequest request) {
        // 1. puuid 발급 가능한지 검증
        String puuid = riotAccountService.getPuuid(request.getGameName(), request.getTag());

        // 2. summonerid 발급 가능한지 검증
        riotAccountService.getSummonerId(puuid);
        return "해당 Riot 계정은 존재합니다";
    }

    @Transactional
    public String join(RiotJoinRequest request) {
        // 1. [Member] puuid 중복 확인
        memberService.checkDuplicateMemberByPuuid(request.getPuuid());

        // 2. [Riot] gameName, Tag 얻기
        RiotPuuidGameNameResponse response = riotAccountService.getAccountByPuuid(request.getPuuid());

        // 3. [Riot] summonerId 얻기
        String summonerId = riotAccountService.getSummonerId(request.getPuuid());

        // 3. [Riot] tier, rank, winrate 얻기
        List<TierDetails> tierWinrateRank = riotInfoService.getTierWinrateRank(summonerId);

        // 4. [Member] member DB에 저장
        Member member = memberService.createMemberRiot(request, response.getGameName(), response.getTagLine(),
                tierWinrateRank);

        // 5. [Riot] 최근 사용한 챔피언 3개 가져오기
        List<ChampionStats> preferChampionStats = riotRecordService.getPreferChampionfromMatch(response.getGameName()
                , response.getPuuid());

        // 6. [Member] Member Champion DB 에서 매핑하기
        memberChampionService.saveMemberChampions(member, preferChampionStats);

        return "RSO 회원가입이 완료되었습니다.";
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
        // 1. 토큰 교환
        RiotAuthTokenResponse riotAuthTokenResponse = riotOAuthService.exchangeCodeForTokens(code);

        // 2. id_token 파싱 → Riot 사용자 정보 추출
        RiotAccountIdResponse summonerInfo = riotOAuthService.getSummonerInfo(riotAuthTokenResponse.getAccessToken());

        // 만약 사용자 정보가 null 일 경우 롤과 연동되지 않은 사용자
        if (summonerInfo == null) {
            return String.format("%s/riot/callback?error=signup_disabled", frontUrl);
        }

        // 3. DB에서 사용자 존재 여부 확인
        List<Member> memberList = memberService.findMemberByPuuid(summonerInfo.getPuuid());

        // 사용자가 아예 없을 경우, 회원가입 요청
        if (memberList.isEmpty()) {
            String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
            String encodedPuuid = URLEncoder.encode(summonerInfo.getPuuid(), StandardCharsets.UTF_8);

            return String.format("%s/riot/callback?puuid=%s&state=%s", frontUrl, encodedPuuid, encodedState);
        }

        // 사용자가 있을 경우, 로그인 진행
        Member member = memberList.get(0);
        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        // refresh token DB에 저장
        authService.addRefreshToken(member, refreshToken);

        return oAuthRedirectBuilder.buildRedirectUrl(member, state, frontUrl, accessToken, refreshToken);
    }

}
