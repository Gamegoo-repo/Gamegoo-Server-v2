package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder

public class BoardByIdResponse {

    long boardId;
    long memberId;
    LocalDateTime createdAt;
    Integer profileImage;
    String gameName;
    String tag;
    Integer mannerLevel;
    Tier tier;
    int rank;
    Mike mike;
    List<ChampionResponse> championResponseList;
    GameMode gameMode;
    Position mainP;
    Position subP;
    Position wantP;
    Integer recentGameCount;
    Double winRate;
    List<Long> gameStyles;
    String contents;

    public static BoardByIdResponse of(Board board) {
        Member poster = board.getMember();
        List<ChampionResponse> championResponseList = poster.getMemberChampionList() == null
                ? List.of()
                : poster.getMemberChampionList().stream()
                .map(mc -> ChampionResponse.of(mc.getChampion()))
                .collect(Collectors.toList());

        List<Long> gameStyleIds = board.getBoardGameStyles().stream()
                .map(bgs -> bgs.getGameStyle().getId())
                .collect(Collectors.toList());

        Tier tier;
        int rank;
        Integer recentGameCount;
        Double winRate;

        if (board.getGameMode() == GameMode.FREE) {
            tier = poster.getFreeTier();
            rank = poster.getFreeRank();
            recentGameCount = poster.getFreeGameCount();
            winRate = poster.getFreeWinRate();
        } else {
            tier = poster.getSoloTier();
            rank = poster.getSoloRank();
            recentGameCount = poster.getSoloGameCount();
            winRate = poster.getSoloWinRate();
        }

        return BoardByIdResponse.builder()
                .boardId(board.getId())
                .memberId(poster.getId())
                .createdAt(board.getCreatedAt())
                .profileImage(poster.getProfileImage())
                .gameName(poster.getGameName())
                .tag(poster.getTag())
                .mannerLevel(poster.getMannerLevel())
                .tier(tier)
                .rank(rank)
                .mike(board.getMike())
                .championResponseList(championResponseList)
                .gameMode(board.getGameMode())
                .mainP(board.getMainP())
                .subP(board.getSubP())
                .wantP(board.getWantP())
                .recentGameCount(recentGameCount)
                .winRate(winRate)
                .gameStyles(gameStyleIds)
                .contents(board.getContent())
                .build();

    }

}
