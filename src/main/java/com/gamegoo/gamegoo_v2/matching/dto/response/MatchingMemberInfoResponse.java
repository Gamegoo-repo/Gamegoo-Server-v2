package com.gamegoo.gamegoo_v2.matching.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.game.dto.response.GameStyleResponse;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class MatchingMemberInfoResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long memberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String matchingUuid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String gameName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String tag;
    @Schema(ref = "#/components/schemas/Tier", requiredMode = Schema.RequiredMode.REQUIRED)
    Tier soloTier;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer soloRank;
    @Schema(ref = "#/components/schemas/Tier", requiredMode = Schema.RequiredMode.REQUIRED)
    Tier freeTier;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer freeRank;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer mannerLevel;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer profileImg;
    @Schema(ref = "#/components/schemas/GameMode", requiredMode = Schema.RequiredMode.REQUIRED)
    GameMode gameMode;
    @Schema(ref = "#/components/schemas/Position", requiredMode = Schema.RequiredMode.REQUIRED)
    Position mainP;
    @Schema(ref = "#/components/schemas/Position", requiredMode = Schema.RequiredMode.REQUIRED)
    Position subP;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    List<Position> wantP;
    @Schema(ref = "#/components/schemas/Mike", requiredMode = Schema.RequiredMode.REQUIRED)
    Mike mike;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<GameStyleResponse> gameStyleResponseList;

    public static MatchingMemberInfoResponse of(Member member, String matchingUuid, GameMode gameMode) {
        List<GameStyleResponse> gameStyleResponseList = member.getMemberGameStyleList().stream()
                .map(memberGameStyle -> GameStyleResponse.of(memberGameStyle.getGameStyle()))
                .toList();

        return MatchingMemberInfoResponse.builder()
                .memberId(member.getId())
                .matchingUuid(matchingUuid)
                .mike(member.getMike())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .soloTier(member.getSoloTier())
                .soloRank(member.getSoloRank())
                .freeTier(member.getFreeTier())
                .freeRank(member.getFreeRank())
                .profileImg(member.getProfileImage())
                .mannerLevel(member.getMannerLevel())
                .mainP(member.getMainP())
                .subP(member.getSubP())
                .wantP(member.getWantP())
                .gameMode(gameMode)
                .gameStyleResponseList(gameStyleResponseList)
                .build();
    }

}
