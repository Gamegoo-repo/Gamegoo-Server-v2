package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.dto.response.MemberRecentStatsResponse;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardByIdResponseForMember {

    long boardId;
    Long memberId;
    Boolean isBlocked;
    Boolean isFriend;
    Long friendRequestMemberId;
    LocalDateTime createdAt;
    Integer profileImage;
    String gameName;
    String tag;
    Integer mannerLevel;
    Double mannerRank;
    Integer mannerRatingCount;
    Tier soloTier;
    int soloRank;
    Tier freeTier;
    int freeRank;
    Mike mike;
    List<ChampionStatsResponse> championStatsResponseList;
    MemberRecentStatsResponse memberRecentStats;
    GameMode gameMode;
    Position mainP;
    Position subP;
    List<Position> wantP;
    Integer recentGameCount;
    Double winRate;
    List<Long> gameStyles;
    String contents;


    public static BoardByIdResponseForMember of(
            Board board,
            boolean isBlocked,
            boolean isFriend,
            Long friendRequestMemberId,
            MannerService mannerService
    ) {
        Member poster = board.getMember();
        List<Long> gameStyleIds = board.getBoardGameStyles().stream()
                .map(bgs -> bgs.getGameStyle().getId())
                .collect(Collectors.toList());

        if (poster == null) { // 비회원 게시글 처리
            return BoardByIdResponseForMember.builder()
                    .boardId(board.getId())
                    .memberId(null)
                    .isBlocked(null)
                    .isFriend(null)
                    .friendRequestMemberId(null)
                    .createdAt(board.getCreatedAt())
                    .profileImage(board.getBoardProfileImage())
                    .gameName(board.getGameName())
                    .tag(board.getTag())
                    .mannerLevel(null)
                    .mannerRank(null)
                    .mannerRatingCount(null)
                    .soloTier(null)
                    .soloRank(0)
                    .freeTier(null)
                    .freeRank(0)
                    .mike(board.getMike())
                    .championStatsResponseList(Collections.emptyList())
                    .memberRecentStats(null)
                    .gameMode(board.getGameMode())
                    .mainP(board.getMainP())
                    .subP(board.getSubP())
                    .wantP(board.getWantP())
                    .recentGameCount(null)
                    .winRate(null)
                    .gameStyles(gameStyleIds)
                    .contents(board.getContent())
                    .build();
        }

        // 회원 게시글 처리
        List<ChampionStatsResponse> championStatsResponseList = poster.getMemberChampionList() == null
                ? List.of()
                : poster.getMemberChampionList().stream()
                .map(mc -> ChampionStatsResponse.builder()
                        .championId(mc.getChampion().getId())
                        .championName(mc.getChampion().getName())
                        .wins(mc.getWins())
                        .games(mc.getGames())
                        .winRate(mc.getGames() > 0 ? (double) mc.getWins() / mc.getGames() : 0)
                        .csPerMinute(mc.getCsPerMinute())
                        .kda(mc.getKDA())
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

        return BoardByIdResponseForMember.builder()
                .boardId(board.getId())
                .memberId(poster.getId())
                .isBlocked(isBlocked)
                .isFriend(isFriend)
                .friendRequestMemberId(friendRequestMemberId)
                .createdAt(board.getCreatedAt())
                .profileImage(poster.getProfileImage())
                .gameName(poster.getGameName())
                .tag(poster.getTag())
                .mannerLevel(poster.getMannerLevel())
                .mannerRank(poster.getMannerRank())
                .mannerRatingCount(mannerService.countMannerRatingByMember(poster, true))
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
