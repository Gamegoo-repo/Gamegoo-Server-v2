package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardUpdateResponse {

    long boardId;
    long memberId;
    Integer profileImage;
    String gameName;
    String tag;
    Tier tier;
    Integer rank;
    Integer gameMode;
    Position mainPosition;
    Position subPosition;
    Position wantPosition;
    Mike mike;
    List<Long> gameStyles;
    String contents;

    public static BoardUpdateResponse of(Board board) {
        Member member = board.getMember();
        List<Long> gameStyleIds = board.getBoardGameStyles().stream()
                .map(bgs -> bgs.getGameStyle().getId())
                .collect(Collectors.toList());

        return BoardUpdateResponse.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .profileImage(board.getBoardProfileImage())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(member.getTier())
                .rank(member.getGameRank())
                .gameMode(board.getMode())
                .mainPosition(board.getMainPosition())
                .subPosition(board.getSubPosition())
                .wantPosition(board.getWantPosition())
                .mike(board.getMike())
                .gameStyles(gameStyleIds)
                .contents(board.getContent())
                .build();
    }

}
