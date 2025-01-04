package com.gamegoo.gamegoo_v2.account.member.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
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
    Boolean mike;
    String gameName;
    String tag;
    Tier tier;
    Integer gameRank;
    Integer mannerLevel;
    Double mannerRank;
    Long mannerRatingCount;  // 매너 평가를 한 사람의 수
    String updatedAt;
    Integer mainP;
    Integer subP;
    Integer wantP;
    Boolean isAgree;
    Boolean isBlind;
    String loginType;
    Double winrate;
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
                .mike(targetMember.isMike())
                .gameName(targetMember.getGameName())
                .tag(targetMember.getTag())
                .tier(targetMember.getTier())
                .gameRank(targetMember.getGameRank())
                .profileImg(targetMember.getProfileImage())
                .mannerLevel(targetMember.getMannerLevel())
                .mannerRank(managerRank)
                .mannerRatingCount(mannerRatingCount)
                .mainP(targetMember.getMainPosition())
                .wantP(targetMember.getWantPosition())
                .subP(targetMember.getSubPosition())
                .isAgree(targetMember.isAgree())
                .isBlind(targetMember.isBlind())
                .winrate(targetMember.getWinRate())
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
