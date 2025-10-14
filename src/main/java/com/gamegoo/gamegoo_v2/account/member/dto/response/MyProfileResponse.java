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

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer profileImg;
    @Schema(ref = "#/components/schemas/Mike", requiredMode = Schema.RequiredMode.REQUIRED)
    Mike mike;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String email;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String gameName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String tag;
    @Schema(ref = "#/components/schemas/Tier", requiredMode = Schema.RequiredMode.REQUIRED)
    Tier soloTier;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer soloRank;
    Double soloWinrate;
    @Schema(ref = "#/components/schemas/Tier", requiredMode = Schema.RequiredMode.REQUIRED)
    Tier freeTier;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer freeRank;
    Double freeWinrate;
    String updatedAt;
    String championStatsRefreshedAt;
    @Schema(ref = "#/components/schemas/Position", requiredMode = Schema.RequiredMode.REQUIRED)
    Position mainP;
    @Schema(ref = "#/components/schemas/Position", requiredMode = Schema.RequiredMode.REQUIRED)
    Position subP;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    List<Position> wantP;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean isAgree;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean isBlind;
    @Schema(ref = "#/components/schemas/LoginType", requiredMode = Schema.RequiredMode.REQUIRED)
    LoginType loginType;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<GameStyleResponse> gameStyleResponseList;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<ChampionStatsResponse> championStatsResponseList;
    MemberRecentStatsResponse memberRecentStats;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean canRefresh;

    public static MyProfileResponse of(Member member) {
        List<GameStyleResponse> gameStyleResponseList = member.getMemberGameStyleList().stream()
                .map(memberGameStyle -> GameStyleResponse.of(memberGameStyle.getGameStyle()))
                .toList();

        List<ChampionStatsResponse> championStatsResponseList = getProfileChampionStats(member);

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

    /**
     * 프로필용 챔피언 통계 조회 (솔랭+자유 통합 통계가 있는 챔피언만)
     */
    public static List<ChampionStatsResponse> getProfileChampionStats(Member member) {
        return member.getMemberChampionList().stream()
                .filter(memberChampion -> memberChampion.getGames() > 0) // 통합 통계가 있는 챔피언만
                .map(ChampionStatsResponse::from)
                .toList();
    }

}
