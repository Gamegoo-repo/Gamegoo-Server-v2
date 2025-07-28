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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardListResponse {

    private Long boardId;
    private Long memberId;
    private String gameName;
    private String tag;
    @Schema(ref = "#/components/schemas/Position")
    private Position mainP;
    @Schema(ref = "#/components/schemas/Position")
    private Position subP;
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    private List<Position> wantP;
    @Schema(ref = "#/components/schemas/Mike")
    private Mike mike;
    private String contents;
    private Integer boardProfileImage;
    private LocalDateTime createdAt;
    private Integer profileImage;
    private Integer mannerLevel;
    @Schema(ref = "#/components/schemas/Tier")
    private Tier tier;
    private int rank;
    @Schema(ref = "#/components/schemas/GameMode")
    private GameMode gameMode;
    private Double winRate;
    private LocalDateTime bumpTime;
    private List<ChampionStatsResponse> championStatsResponseList;
    private MemberRecentStatsResponse memberRecentStats;
    @Schema(ref = "#/components/schemas/Tier")
    private Tier freeTier;
    private int freeRank;
    @Schema(ref = "#/components/schemas/Tier")
    private Tier soloTier;
    private int soloRank;

    public static BoardListResponse of(Board board) {
        Member member = board.getMember();

        // 모든 게시글은 임시 멤버든 정식 멤버든 항상 Member를 가짐
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
                        .averageCs(mc.getGames() > 0 ? (double) mc.getTotalCs() / mc.getGames() : 0)
                        .kda(mc.getKDA())
                        .kills(mc.getGames() > 0 ? (double) mc.getKills() / mc.getGames() : 0)
                        .deaths(mc.getGames() > 0 ? (double) mc.getDeaths() / mc.getGames() : 0)
                        .assists(mc.getGames() > 0 ? (double) mc.getAssists() / mc.getGames() : 0)
                        .build())
                .collect(Collectors.toList());

        return BoardListResponse.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .mainP(board.getMainP())
                .subP(board.getSubP())
                .wantP(board.getWantP())
                .mike(board.getMike())
                .contents(board.getContent())
                .boardProfileImage(member.getProfileImage())
                .createdAt(board.getCreatedAt())
                .profileImage(member.getProfileImage())
                .mannerLevel(member.getMannerLevel())
                .tier(tier)
                .rank(rank)
                .gameMode(board.getGameMode())
                .winRate(winRate)
                .bumpTime(board.getBumpTime())
                .championStatsResponseList(championStatsResponseList)
                .memberRecentStats(MemberRecentStatsResponse.from(member.getMemberRecentStats()))
                .freeTier(member.getFreeTier())
                .freeRank(member.getFreeRank())
                .soloTier(member.getSoloTier())
                .soloRank(member.getSoloRank())
                .build();
    }

}
