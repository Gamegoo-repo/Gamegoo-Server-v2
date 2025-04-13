package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAuthTokenResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAccountIdResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotPuuidGameNameResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.request.RiotVerifyExistUserRequest;
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
    private final RiotAuthService riotAuthService;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

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
    public String processOAuthCallback(String code, String state) {
        // 1. 토큰 교환
        RiotAuthTokenResponse riotAuthTokenResponse = riotOAuthService.exchangeCodeForTokens(code);

        // 2. id_token 파싱 → Riot 사용자 정보 추출
        RiotAccountIdResponse summonerInfo = riotOAuthService.getSummonerInfo(riotAuthTokenResponse.getAccessToken());

        // 3. puuid로 사용자 정보 얻기
        RiotPuuidGameNameResponse accountByPuuid = riotAuthService.getAccountByPuuid(summonerInfo.getPuuid());

        // 3. DB에서 사용자 존재 여부 확인
        List<Member> memberList = memberService.findMemberByGameNameAndTag(accountByPuuid.getGameName(),
                accountByPuuid.getTagLine());

        // 사용자가 아예 없을 경우, 회원가입 요청
        if (memberList.isEmpty()) {
            String encodedGameName = URLEncoder.encode(accountByPuuid.getGameName(), StandardCharsets.UTF_8);
            String encodedTag = URLEncoder.encode(accountByPuuid.getTagLine(), StandardCharsets.UTF_8);
            String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
            String encodedPuuid = URLEncoder.encode(accountByPuuid.getPuuid(), StandardCharsets.UTF_8);

            return String.format("%s/rso/callback?puuid=%s&gameName=%s&tag=%s&state=%s",
                    frontUrl, encodedPuuid, encodedGameName, encodedTag, encodedState);
        }

        // 사용자가 있을 경우, 로그인 진행
        Member member = memberList.get(0);
        String accessToken = jwtProvider.createAccessToken(member.getId());

        return String.format("%s/rso/callback?accessToken=%s&state=%s", frontUrl, accessToken, state);
    }

}
