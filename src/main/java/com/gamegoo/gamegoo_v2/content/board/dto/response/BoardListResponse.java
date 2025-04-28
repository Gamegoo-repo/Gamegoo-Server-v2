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
public class BoardListResponse {

    long boardId;
    long memberId;
    Integer profileImage;
    String gameName;
    String tag;
    Integer mannerLevel;
    Tier tier;
    int rank;
    GameMode gameMode;
    Position mainP;
    Position subP;
    List<Position> wantP;
    private List<ChampionStatsResponse> championStatsResponseList;
    Double winRate;
    LocalDateTime createdAt;
    LocalDateTime bumpTime;
    String contents;
    Mike mike;

    public static BoardListResponse of(Board board) {
        Member member = board.getMember();
        Tier tier;
        int rank;
        Double winRate;
        if (board.getGameMode() == GameMode.FREE) {
            tier = member.getFreeTier();
            rank = member.getFreeRank();
            winRate = member.getFreeWinRate();
        } else {
            tier = member.getSoloTier();
            rank = member.getSoloRank();
            winRate = member.getSoloWinRate();
        }

        List<ChampionStatsResponse> championStatsResponseList = member.getMemberChampionList() == null
                ? List.of()
                : member.getMemberChampionList().stream()
                .map(mc -> ChampionStatsResponse.builder()
                        .championId(mc.getChampion().getId())
                        .championName(mc.getChampion().getName())
                        .wins(mc.getWins())
                        .games(mc.getGames())
                        .winRate(mc.getGames() > 0 ? (double) mc.getWins() / mc.getGames() : 0)
                        .csPerMinute(mc.getCsPerMinute())
                        .build())
                .collect(Collectors.toList());

        return BoardListResponse.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .profileImage(member.getProfileImage())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .mannerLevel(member.getMannerLevel())
                .tier(tier)
                .rank(rank)
                .gameMode(board.getGameMode())
                .mainP(board.getMainP())
                .subP(board.getSubP())
                .wantP(board.getWantP())
                .championStatsResponseList(championStatsResponseList)
                .winRate(winRate)
                .createdAt(board.getCreatedAt())
                .bumpTime(board.getBumpTime())
                .contents(board.getContent())
                .mike(board.getMike())
                .build();
    }

}
