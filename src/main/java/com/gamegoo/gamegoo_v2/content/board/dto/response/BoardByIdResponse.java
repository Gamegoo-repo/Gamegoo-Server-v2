package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.dto.response.MemberRecentStatsResponse;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardByIdResponse {

    long boardId;
    Long memberId;
    LocalDateTime createdAt;
    Integer profileImage;
    String gameName;
    String tag;
    Integer mannerLevel;
    @Schema(ref = "#/components/schemas/Tier")
    Tier soloTier;
    int soloRank;
    @Schema(ref = "#/components/schemas/Tier")
    Tier freeTier;
    int freeRank;
    @Schema(ref = "#/components/schemas/Mike")
    Mike mike;
    List<ChampionStatsResponse> championStatsResponseList;
    MemberRecentStatsResponse memberRecentStats;
    @Schema(ref = "#/components/schemas/GameMode")
    GameMode gameMode;
    @Schema(ref = "#/components/schemas/Position")
    Position mainP;
    @Schema(ref = "#/components/schemas/Position")
    Position subP;
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    List<Position> wantP;
    Integer recentGameCount;
    Double winRate;
    List<Long> gameStyles;
    String contents;

    public static BoardByIdResponse of(Board board) {
        Member poster = board.getMember();
        List<Long> gameStyleIds = board.getBoardGameStyles().stream()
                .map(bgs -> bgs.getGameStyle().getId())
                .collect(Collectors.toList());
        List<ChampionStatsResponse> championStatsResponseList = poster.getMemberChampionList() == null
                ? List.of()
                : poster.getMemberChampionList().stream()
                .map(mc -> ChampionStatsResponse.builder()
                        .championId(mc.getChampion().getId())
                        .championName(mc.getChampion().getName())
                        .wins(mc.getWins())
                        .games(mc.getGames())
                        .winRate(mc.getGames() > 0 ? ((double) mc.getWins() / mc.getGames()) * 100 : 0)
                        .csPerMinute(mc.getCsPerMinute())
                        .averageCs(mc.getGames() > 0 ? (double) mc.getTotalCs() / mc.getGames() : 0)
                        .kda(mc.getKDA())
                        .kills(mc.getGames() > 0 ? (double) mc.getKills() / mc.getGames() : 0)
                        .deaths(mc.getGames() > 0 ? (double) mc.getDeaths() / mc.getGames() : 0)
                        .assists(mc.getGames() > 0 ? (double) mc.getAssists() / mc.getGames() : 0)
                        .build())
                .collect(Collectors.toList());

        Integer recentGameCount;
        Double winRate;

        if (board.getGameMode() == GameMode.FREE) {
            recentGameCount = poster.getFreeGameCount();
            winRate = poster.getFreeWinRate();
        } else {
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
                .soloTier(poster.getSoloTier())
                .soloRank(poster.getSoloRank())
                .freeTier(poster.getFreeTier())
                .freeRank(poster.getFreeRank())
                .mike(board.getMike())
                .championStatsResponseList(championStatsResponseList)
                .memberRecentStats(MemberRecentStatsResponse.from(poster.getMemberRecentStats()))
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
