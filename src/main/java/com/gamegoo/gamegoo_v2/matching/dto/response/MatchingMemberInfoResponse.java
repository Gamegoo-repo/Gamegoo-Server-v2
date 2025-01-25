package com.gamegoo.gamegoo_v2.matching.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.game.dto.response.GameStyleResponse;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MatchingMemberInfoResponse {

    Long memberId;
    String matchingUuid;
    String gameName;
    String tag;
    Tier tier;
    Integer rank;
    Integer mannerLevel;
    Integer profileImg;
    GameMode gameMode;
    Position mainP;
    Position subP;
    Position wantP;
    Mike mike;
    List<GameStyleResponse> gameStyleResponseList;

    public static MatchingMemberInfoResponse of(Member member, String matchingUuid) {
        List<GameStyleResponse> gameStyleResponseList = member.getMemberGameStyleList().stream()
                .map(memberGameStyle -> GameStyleResponse.of(memberGameStyle.getGameStyle()))
                .toList();

        return MatchingMemberInfoResponse.builder()
                .memberId(member.getId())
                .matchingUuid(matchingUuid)
                .mike(member.getMike())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(member.getTier())
                .rank(member.getGameRank())
                .profileImg(member.getProfileImage())
                .mannerLevel(member.getMannerLevel())
                .mainP(member.getMainPosition())
                .subP(member.getSubPosition())
                .wantP(member.getWantPosition())
                .gameStyleResponseList(gameStyleResponseList)
                .build();
    }

}
