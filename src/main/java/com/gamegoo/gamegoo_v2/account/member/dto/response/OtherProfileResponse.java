package com.gamegoo.gamegoo_v2.account.member.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.game.dto.response.ChampionResponse;
import com.gamegoo.gamegoo_v2.game.dto.response.GameStyleResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class OtherProfileResponse {

    Long id;
    Integer profileImg;
    Mike mike;
    String gameName;
    String tag;
    Tier soloTier;
    Integer soloRank;
    Double soloWinrate;
    Tier freeTier;
    Integer freeRank;
    Double freeWinrate;
    Integer mannerLevel;
    Double mannerRank;
    Long mannerRatingCount;  // 매너 평가를 한 사람의 수
    String updatedAt;
    Position mainP;
    Position subP;
    Position wantP;
    Boolean isAgree;
    Boolean isBlind;
    String loginType;
    Boolean blocked; // 해당 회원을 차단했는지 여부
    Boolean friend; // 해당 회원과의 친구 여부
    Long friendRequestMemberId; // 해당 회원과의 친구 요청 상태
    List<GameStyleResponse> gameStyleResponseList;
    List<ChampionResponse> championResponseList;

    public static OtherProfileResponse of(Member targetMember, Double managerRank,
                                          Long mannerRatingCount, Boolean isFriend, Long friendRequestMemberId,
                                          Boolean isBlocked) {
        List<GameStyleResponse> gameStyleResponseList = targetMember.getMemberGameStyleList().stream()
                .map(memberGameStyle -> GameStyleResponse.of(memberGameStyle.getGameStyle()))
                .toList();

        List<ChampionResponse> championResponseList = targetMember.getMemberChampionList().stream()
                .map(memberChampion -> ChampionResponse.of(memberChampion.getChampion()))
                .toList();

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
                .mannerLevel(targetMember.getMannerLevel())
                .mannerRank(managerRank)
                .mannerRatingCount(mannerRatingCount)
                .mainP(targetMember.getMainP())
                .wantP(targetMember.getWantP())
                .subP(targetMember.getSubP())
                .isAgree(targetMember.isAgree())
                .isBlind(targetMember.isBlind())
                .loginType(String.valueOf(targetMember.getLoginType()))
                .updatedAt(String.valueOf(targetMember.getUpdatedAt()))
                .blocked(isBlocked)
                .friend(isFriend)
                .friendRequestMemberId(friendRequestMemberId)
                .gameStyleResponseList(gameStyleResponseList)
                .championResponseList(championResponseList)
                .build();
    }

}
