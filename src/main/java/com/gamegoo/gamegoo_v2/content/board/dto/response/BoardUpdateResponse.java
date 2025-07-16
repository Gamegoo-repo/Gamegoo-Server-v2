package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
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
    GameMode gameMode;
    Position mainP;
    Position subP;
    List<Position> wantP;
    Mike mike;
    List<Long> gameStyles;
    String contents;

    public static BoardUpdateResponse of(Board board) {
        Member member = board.getMember();
        List<Long> gameStyleIds = board.getBoardGameStyles().stream()
                .map(bgs -> bgs.getGameStyle().getId())
                .collect(Collectors.toList());

        if (member == null) {
            return BoardUpdateResponse.builder()
                    .boardId(board.getId())
                    .memberId(0L)
                    .profileImage(board.getBoardProfileImage())
                    .gameName(board.getGameName())
                    .tag(board.getTag())
                    .tier(null)
                    .rank(0)
                    .gameMode(board.getGameMode())
                    .mainP(board.getMainP())
                    .subP(board.getSubP())
                    .wantP(board.getWantP())
                    .mike(board.getMike())
                    .gameStyles(gameStyleIds)
                    .contents(board.getContent())
                    .build();
        }

        Tier tier;
        int rank;
        if (board.getGameMode() == GameMode.FREE) {
            tier = member.getFreeTier();
            rank = member.getFreeRank();
        } else {
            tier = member.getSoloTier();
            rank = member.getSoloRank();
        }

        return BoardUpdateResponse.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .profileImage(member.getProfileImage())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(tier)
                .rank(rank)
                .gameMode(board.getGameMode())
                .mainP(board.getMainP())
                .subP(board.getSubP())
                .wantP(board.getWantP())
                .mike(board.getMike())
                .gameStyles(gameStyleIds)
                .contents(board.getContent())
                .build();
    }

}
