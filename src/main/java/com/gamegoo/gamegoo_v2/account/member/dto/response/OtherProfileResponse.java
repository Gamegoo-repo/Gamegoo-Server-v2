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

@Builder
@Getter
public class OtherProfileResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer profileImg;
    @Schema(ref = "#/components/schemas/Mike", requiredMode = Schema.RequiredMode.REQUIRED)
    Mike mike;
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
    Boolean blocked; // 해당 회원을 차단했는지 여부
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean friend; // 해당 회원과의 친구 여부
    Long friendRequestMemberId; // 해당 회원과의 친구 요청 상태
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<GameStyleResponse> gameStyleResponseList;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<ChampionStatsResponse> championStatsResponseList;
    MemberRecentStatsResponse memberRecentStats;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean canRefresh;

    public static OtherProfileResponse of(Member targetMember, Boolean isFriend, Long friendRequestMemberId,
                                          Boolean isBlocked) {
        List<GameStyleResponse> gameStyleResponseList = targetMember.getMemberGameStyleList().stream()
                .map(memberGameStyle -> GameStyleResponse.of(memberGameStyle.getGameStyle()))
                .toList();

        List<ChampionStatsResponse> championStatsResponseList = MyProfileResponse.getProfileChampionStats(targetMember);

        // 3일 기준으로 갱신 가능 여부 체크
        boolean canRefresh = targetMember.canRefreshChampionStats();

        return OtherProfileResponse.builder()
                .id(targetMember.getId())
                .mike(targetMember.getMike())
                .gameName(targetMember.getGameName())
                .tag(targetMember.getTag())
                .soloTier(targetMember.getSoloTier())
                .soloRank(targetMember.getSoloRank())
                .soloWinrate(targetMember.getSoloWinRate())
                .freeTier(targetMember.getFreeTier())
                .freeRank(targetMember.getFreeRank())
                .freeWinrate(targetMember.getFreeWinRate())
                .profileImg(targetMember.getProfileImage())
                .mainP(targetMember.getMainP())
                .wantP(targetMember.getWantP())
                .subP(targetMember.getSubP())
                .isAgree(targetMember.isAgree())
                .isBlind(targetMember.getBlind())
                .loginType(targetMember.getLoginType())
                .updatedAt(String.valueOf(targetMember.getUpdatedAt()))
                .blocked(isBlocked)
                .friend(isFriend)
                .friendRequestMemberId(friendRequestMemberId)
                .gameStyleResponseList(gameStyleResponseList)
                .championStatsResponseList(championStatsResponseList)
                .memberRecentStats(MemberRecentStatsResponse.from(targetMember.getMemberRecentStats()))
                .canRefresh(canRefresh)
                .build();
    }


}
