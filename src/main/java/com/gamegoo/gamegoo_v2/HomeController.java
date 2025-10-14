package com.gamegoo.gamegoo_v2;

import com.gamegoo.gamegoo_v2.account.auth.service.AuthFacadeService;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.account.member.service.ChampionStatsRefreshService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.config.swagger.ApiErrorCodes;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import com.gamegoo.gamegoo_v2.external.riot.dto.request.RiotJoinRequest;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotAuthService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "테스트 편의를 위한 API")
@RequiredArgsConstructor
@RestController
public class HomeController {

    private final RiotFacadeService riotFacadeService;
    private final AuthFacadeService authFacadeService;
    private final RiotAuthService riotAuthService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final ChampionStatsRefreshService championStatsRefreshService;

    @Operation(summary = "홈 엔드포인트", description = "API 서비스 상태를 확인합니다.")
    @GetMapping("/home")
    @ApiErrorCodes({ErrorCode._INTERNAL_SERVER_ERROR})
    public ApiResponse<String> home() {
        return ApiResponse.ok("Gamegoo V2 API 서비스 입니다. 환영합니다.");
    }

    @Operation(summary = "에러 테스트", description = "예외를 발생시켜 테스트합니다.")
    @GetMapping("/errortest")
    @ApiErrorCodes({ErrorCode._BAD_REQUEST})
    public ApiResponse<Object> error() {
        throw new GlobalException(ErrorCode._BAD_REQUEST);
    }

    @Operation(summary = "Health Check", description = "health check를 위한 API 입니다.")
    @GetMapping("/healthcheck")
    @ApiErrorCodes({ErrorCode._INTERNAL_SERVER_ERROR})
    public ApiResponse<String> healthcheck() {
        return ApiResponse.ok("OK");
    }

    @Operation(summary = "라이엇 계정 회원 가입")
    @PostMapping("/home/join")
    @ApiErrorCodes({
            ErrorCode.MEMBER_ALREADY_EXISTS,
            ErrorCode.RIOT_INVALID_API_KEY,
            ErrorCode.RIOT_NOT_FOUND,
            ErrorCode.RIOT_API_ERROR,
            ErrorCode.RIOT_SERVER_ERROR,
            ErrorCode.RIOT_NETWORK_ERROR,
            ErrorCode.RIOT_UNKNOWN_ERROR
    })
    public ApiResponse<Object> joinTest(@RequestBody RiotUserInfo riotUserInfo) {
        String puuid = riotAuthService.getPuuid(riotUserInfo.getGamename(), riotUserInfo.getTag()); // puuid 조회
        RiotJoinRequest request = new RiotJoinRequest(puuid, true);
        riotFacadeService.join(request);// 회원 가입
        Member member = memberRepository.findByPuuid(puuid).get(0);
        return ApiResponse.ok(new JoinTestResponse(member.getId(), puuid));
    }

    @Operation(summary = "챔피언 전적 통계 갱신")
    @GetMapping("/home/refresh/stats/{memberId}")
    @Parameter(name = "memberId", description = "대상 회원의 id 입니다.")
    @ApiErrorCodes({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode._INTERNAL_SERVER_ERROR
    })
    public ApiResponse<String> refreshStats(@PathVariable Long memberId) {
        Member member = memberService.findMemberById(memberId);
        championStatsRefreshService.refreshChampionStats(member);
        return ApiResponse.ok("UPDATED");
    }

    @Operation(summary = "소환사명과 태그로 해당 회원 id 조회")
    @PostMapping("/home/getMemberId")
    @ApiErrorCodes({ErrorCode.MEMBER_NOT_FOUND})
    public ApiResponse<Long> getMemberId(@RequestBody RiotUserInfo riotUserInfo) {
        Member member = memberService.findMemberByGameNameAndTag(riotUserInfo.getGamename(),
                riotUserInfo.getTag());
        return ApiResponse.ok(member.getId());
    }

    @GetMapping("/home/token/{memberId}")
    @Operation(summary = "memberId로 access token 발급 API", description = "테스트용으로 access token을 발급받을 수 있는 API 입니다.")
    @Parameter(name = "memberId", description = "대상 회원의 id 입니다.")
    @ApiErrorCodes({ErrorCode.MEMBER_NOT_FOUND})
    public ApiResponse<String> getTestAccessToken(@PathVariable(name = "memberId") Long memberId) {
        return ApiResponse.ok(authFacadeService.createTestAccessToken(memberId));
    }

    @Getter
    public static class RiotUserInfo {

        String gamename;
        String tag;

    }

    @Getter
    @AllArgsConstructor
    public static class JoinTestResponse {

        Long memberId;
        String puuid;

    }

}
