package com.gamegoo.gamegoo_v2.account.member.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.game.dto.response.GameStyleResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.ChampionStatsResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyProfileResponse {

    Long id;
    Integer profileImg;
    @Schema(ref = "#/components/schemas/Mike")
    Mike mike;
    String email;
    String gameName;
    String tag;
    @Schema(ref = "#/components/schemas/Tier")
    Tier soloTier;
    Integer soloRank;
    Double soloWinrate;
    @Schema(ref = "#/components/schemas/Tier")
    Tier freeTier;
    Integer freeRank;
    Double freeWinrate;
    String updatedAt;
    String championStatsRefreshedAt;
    @Schema(ref = "#/components/schemas/Position")
    Position mainP;
    @Schema(ref = "#/components/schemas/Position")
    Position subP;
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    List<Position> wantP;
    Boolean isAgree;
    Boolean isBlind;
    @Schema(ref = "#/components/schemas/LoginType")
    LoginType loginType;
    List<GameStyleResponse> gameStyleResponseList;
    List<ChampionStatsResponse> championStatsResponseList;
    MemberRecentStatsResponse memberRecentStats;
    Boolean canRefresh;

    public static MyProfileResponse of(Member member) {
        List<GameStyleResponse> gameStyleResponseList = member.getMemberGameStyleList().stream()
                .map(memberGameStyle -> GameStyleResponse.of(memberGameStyle.getGameStyle()))
                .toList();

        List<ChampionStatsResponse> championStatsResponseList = member.getMemberChampionList().stream()
                .map(ChampionStatsResponse::from)
                .toList();

        // 3일 기준으로 갱신 가능 여부 체크
        boolean canRefresh = member.canRefreshChampionStats();

        return MyProfileResponse.builder()
                .id(member.getId())
                .mike(member.getMike())
                .email(member.getEmail())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .soloTier(member.getSoloTier())
                .soloRank(member.getSoloRank())
                .soloWinrate(member.getSoloWinRate())
                .freeTier(member.getFreeTier())
                .freeRank(member.getFreeRank())
                .freeWinrate(member.getFreeWinRate())
                .profileImg(member.getProfileImage())
                .mainP(member.getMainP())
                .subP(member.getSubP())
                .wantP(member.getWantP())
                .isAgree(member.isAgree())
                .isBlind(member.getBlind())
                .loginType(member.getLoginType())
                .updatedAt(member.getUpdatedAt() != null ? member.getUpdatedAt().toString() : null)
                .championStatsRefreshedAt(member.getChampionStatsRefreshedAt() != null ? member.getChampionStatsRefreshedAt().toString() : null)
                .gameStyleResponseList(gameStyleResponseList)
                .championStatsResponseList(championStatsResponseList)
                .memberRecentStats(MemberRecentStatsResponse.from(member.getMemberRecentStats()))
                .canRefresh(canRefresh)
                .build();
    }


}
