package com.gamegoo.gamegoo_v2.matching.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.game.dto.response.GameStyleResponse;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class MatchingMemberInfoResponse {

    Long memberId;
    String matchingUuid;
    String gameName;
    String tag;
    Tier soloTier;
    Integer soloRank;
    Tier freeTier;
    Integer freeRank;
    Integer mannerLevel;
    Integer profileImg;
    GameMode gameMode;
    Position mainP;
    Position subP;
    List<Position> wantP;
    Mike mike;
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
