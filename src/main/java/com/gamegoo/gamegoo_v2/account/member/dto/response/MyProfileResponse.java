package com.gamegoo.gamegoo_v2.account.member.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.game.dto.response.ChampionResponse;
import com.gamegoo.gamegoo_v2.game.dto.response.GameStyleResponse;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyProfileResponse {

    Long id;
    Integer profileImg;
    Mike mike;
    String email;
    String gameName;
    String tag;
    Tier tier;
    Integer gameRank;
    Double mannerRank;
    Integer mannerLevel;
    String updatedAt;
    Position mainP;
    Position subP;
    Position wantP;
    Boolean isAgree;
    Boolean isBlind;
    String loginType;
    Double winrate;
    List<GameStyleResponse> gameStyleResponseList;
    List<ChampionResponse> championResponseList;

    public static MyProfileResponse of(Member member, Double mannerRank) {
        List<GameStyleResponse> gameStyleResponseList = member.getMemberGameStyleList().stream()
                .map(memberGameStyle -> GameStyleResponse.of(memberGameStyle.getGameStyle()))
                .toList();

        List<ChampionResponse> championResponseList = member.getMemberChampionList().stream()
                .map(memberChampion -> ChampionResponse.of(memberChampion.getChampion()))
                .toList();

        return MyProfileResponse.builder()
                .id(member.getId())
                .mike(member.getMike())
                .email(member.getEmail())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(member.getSoloTier())
                .gameRank(member.getSoloRank())
                .profileImg(member.getProfileImage())
                .mannerLevel(member.getMannerLevel())
                .mannerRank(mannerRank)
                .mainP(member.getMainPosition())
                .subP(member.getSubPosition())
                .wantP(member.getWantPosition())
                .isAgree(member.isAgree())
                .isBlind(member.isBlind())
                .winrate(member.getSoloWinRate())
                .loginType(String.valueOf(member.getLoginType()))
                .updatedAt(String.valueOf(member.getUpdatedAt()))
                .gameStyleResponseList(gameStyleResponseList)
                .championResponseList(championResponseList)
                .build();
    }

}
