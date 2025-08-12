package com.gamegoo.gamegoo_v2;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.ChampionStatsRefreshService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import com.gamegoo.gamegoo_v2.external.riot.dto.request.RiotJoinRequest;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotAuthService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "테스트 편의를 위한 API")
@RequiredArgsConstructor
@RestController
public class HomeController {

    private final RiotFacadeService riotFacadeService;
    private final RiotAuthService riotAuthService;
    private final ChampionStatsRefreshService championStatsRefreshService;

    @Operation(summary = "홈 엔드포인트", description = "API 서비스 상태를 확인합니다.")
    @GetMapping("/home")
    public ApiResponse<String> home() {
        return ApiResponse.ok("Gamegoo V2 API 서비스 입니다. 환영합니다.");
    }

    @Operation(summary = "에러 테스트", description = "예외를 발생시켜 테스트합니다.")
    @GetMapping("/errortest")
    public ApiResponse<Object> error() {
        throw new GlobalException(ErrorCode._BAD_REQUEST);
    }

    @Operation(summary = "Health Check", description = "health check를 위한 API 입니다.")
    @GetMapping("/healthcheck")
    public ApiResponse<String> healthcheck() {
        return ApiResponse.ok("OK");
    }

    @Operation(summary = "라이엇 계정 회원 가입")
    @PostMapping("/join")
    public ApiResponse<Object> joinTest(@RequestBody RiotUserInfo riotUserInfo) {
        String puuid = riotAuthService.getPuuid(riotUserInfo.getGamename(), riotUserInfo.getTag()); // puuid 조회
        RiotJoinRequest request = new RiotJoinRequest(puuid, true);
        Member member = riotFacadeService.join(request);// 회원 가입
        championStatsRefreshService.refreshChampionStats(member); // memberRecentStats 갱신
        return ApiResponse.ok(new JoinTestResponse(member.getId(), puuid));

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
