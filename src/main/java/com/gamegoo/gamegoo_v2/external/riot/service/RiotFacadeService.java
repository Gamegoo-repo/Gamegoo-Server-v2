package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.account.auth.dto.response.LoginResponse;
import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.auth.service.AuthService;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RSOResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAuthTokenResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAccountIdResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotPuuidGameNameResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.request.RiotVerifyExistUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AuthService authService;

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
    public RSOResponse processOAuthCallback(String code, String state) {
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
            return RSOResponse.builder()
                    .isMember(false)
                    .build();
        }

        // 사용자가 있을 경우, 로그인 진행
        Member member = memberList.get(0);

        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        // 5. refresh Token DB에 저장
        authService.addRefreshToken(member, refreshToken);

        // 4. 프론트 리다이렉트 주소 복원

        return RSOResponse.builder()
                .isMember(true)
                .loginResponse(LoginResponse.of(member, accessToken, refreshToken))
                .build();
    }

}
