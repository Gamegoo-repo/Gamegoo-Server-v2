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

@Getter
@Builder
public class MyProfileResponse {

    Long id;
    Integer profileImg;
    Mike mike;
    String email;
    String gameName;
    String tag;
    Tier soloTier;
    Integer soloRank;
    Double soloWinrate;
    Tier freeTier;
    Integer freeRank;
    Double freeWinrate;
    String updatedAt;
    Position mainP;
    Position subP;
    List<Position> wantP;
    Boolean isAgree;
    Boolean isBlind;
    String loginType;
    List<GameStyleResponse> gameStyleResponseList;
    List<ChampionResponse> championResponseList;

    public static MyProfileResponse of(Member member) {
        List<GameStyleResponse> gameStyleResponseList = member.getMemberGameStyleList().stream()
                .map(memberGameStyle -> GameStyleResponse.of(memberGameStyle.getGameStyle()))
                .toList();

        List<ChampionResponse> championResponseList = member.getMemberChampionList().stream()
                .map(memberChampion -> ChampionResponse.of(
                        memberChampion.getChampion(),
                        memberChampion.getWins(),
                        memberChampion.getGames()))
                .toList();

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
                .loginType(String.valueOf(member.getLoginType()))
                .updatedAt(String.valueOf(member.getUpdatedAt()))
                .gameStyleResponseList(gameStyleResponseList)
                .championResponseList(championResponseList)
                .build();
    }

}
