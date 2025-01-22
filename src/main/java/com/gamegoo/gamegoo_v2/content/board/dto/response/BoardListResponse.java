package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.game.dto.response.ChampionResponse;
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
    int gameMode;
    Position mainPosition;
    Position subPosition;
    Position wantPosition;
    List<ChampionResponse> championResponseList;
    Double winRate;
    LocalDateTime createdAt;
    Mike mike;

    public static BoardListResponse of(Board board) {
        Member member = board.getMember();
        List<ChampionResponse> championResponseList = (member.getMemberChampionList() != null) ?
                member.getMemberChampionList().stream()
                        .map(mc -> ChampionResponse.of(mc.getChampion()))
                        .collect(Collectors.toList())
                : null;

        return BoardListResponse.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .profileImage(board.getBoardProfileImage())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .mannerLevel(member.getMannerLevel())
                .tier(member.getTier())
                .rank(member.getGameRank())
                .gameMode(board.getMode())
                .mainPosition(board.getMainPosition())
                .subPosition(board.getSubPosition())
                .wantPosition(board.getWantPosition())
                .championResponseList(championResponseList)
                .winRate(member.getWinRate())
                .createdAt(board.getCreatedAt())
                .mike(board.getMike())
                .build();
    }

}
