package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
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
    Tier tier;
    int rank;
    boolean mike;
    List<ChampionResponse> championResponseDTOList;
    int gameMode;
    int mainPosition;
    int subPosition;
    int wantPosition;
    Integer recentGameCount;
    Double winRate;
    List<Long> gameStyles;
    String contents;


    public static BoardByIdResponseForMember of(
            Board board,
            Member viewer,       // 또는 viewerId
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
                .tier(poster.getTier())
                .rank(poster.getGameRank())
                .mike(board.isMike())
                .championResponseDTOList(championResponseList)
                .gameMode(board.getMode())
                .mainPosition(board.getMainPosition())
                .subPosition(board.getSubPosition())
                .wantPosition(board.getWantPosition())
                .recentGameCount(poster.getGameCount())
                .winRate(poster.getWinRate())
                .gameStyles(board.getBoardGameStyles().stream()
                        .map(bgs -> bgs.getGameStyle().getId())
                        .toList())
                .contents(board.getContent())
                .build();
    }

}
