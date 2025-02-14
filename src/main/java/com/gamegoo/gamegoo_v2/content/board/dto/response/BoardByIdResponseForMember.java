package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.social.manner.domain.MannerKeyword;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder

public class BoardByIdResponseForMember {

    long boardId;
    long memberId;
    Boolean isBlocked;
    Boolean isFriend;
    Long friendRequestMemberId;
    LocalDateTime createdAt;
    Integer profileImage;
    String gameName;
    String tag;
    Integer mannerLevel;
    List<MannerKeyword> mannerKeywords;
    Tier soloTier;
    Tier freeTier;
    int soloRank;
    int freeRank;
    Mike mike;
    List<ChampionResponse> championResponseDTOList;
    GameMode gameMode;
    Position mainP;
    Position subP;
    Position wantP;
    Integer soloRecentGameCount;
    Integer freeRecentGameCount;
    Double soloWinRate;
    Double freeWinRate;
    List<Long> gameStyles;
    String contents;


    public static BoardByIdResponseForMember of(
            Board board,
            boolean isBlocked,
            boolean isFriend,
            Long friendRequestMemberId
    ) {
        Member poster = board.getMember();

        List<ChampionResponse> championResponseList = poster.getMemberChampionList() == null
                ? List.of()
                : poster.getMemberChampionList().stream()
                .map(mc -> ChampionResponse.of(mc.getChampion()))
                .collect(Collectors.toList());


        List<Long> gameStyleIds = board.getBoardGameStyles().stream()
                .map(bgs -> bgs.getGameStyle().getId())
                .collect(Collectors.toList());

        return BoardByIdResponseForMember.builder()
                .boardId(board.getId())
                .memberId(poster.getId())
                .isBlocked(isBlocked)
                .isFriend(isFriend)
                .friendRequestMemberId(friendRequestMemberId)
                .createdAt(board.getCreatedAt())
                .profileImage(board.getBoardProfileImage())
                .gameName(poster.getGameName())
                .tag(poster.getTag())
                .mannerLevel(poster.getMannerLevel())
                .soloTier(poster.getSoloTier())
                .freeTier(poster.getFreeTier())
                .soloRank(poster.getSoloRank())
                .freeRank(poster.getFreeRank())
                .mike(board.getMike())
                .championResponseDTOList(championResponseList)
                .gameMode(board.getGameMode())
                .mainP(board.getMainP())
                .subP(board.getSubP())
                .wantP(board.getWantP())
                .soloRecentGameCount(poster.getSoloGameCount())
                .freeRecentGameCount(poster.getFreeGameCount())
                .soloWinRate(poster.getSoloWinRate())
                .freeWinRate(poster.getFreeWinRate())
                .gameStyles(gameStyleIds)
                .contents(board.getContent())
                .build();
    }

}
