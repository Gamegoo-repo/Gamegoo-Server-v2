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

@Getter
@Builder
public class BoardInsertResponse {

    private long boardId;
    private long memberId;
    private Integer profileImage;
    private String gameName;
    private String tag;
    private Tier tier;
    private int rank;
    private GameMode gameMode;
    private Position mainP;
    private Position subP;
    private List<Position> wantP;
    private Mike mike;
    private List<Long> gameStyles;
    private String contents;

    public static BoardInsertResponse of(Board board, Member member) {

        Tier tier;
        int rank;
        if (board.getGameMode() == GameMode.FREE) {
            tier = member.getFreeTier();
            rank = member.getFreeRank();
        } else {
            tier = member.getSoloTier();
            rank = member.getSoloRank();
        }

        return BoardInsertResponse.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .profileImage(board.getBoardProfileImage())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(tier)
                .rank(rank)
                .gameMode(board.getGameMode())
                .mainP(board.getMainP())
                .subP(board.getSubP())
                .wantP(board.getWantP())
                .mike(board.getMike())
                .gameStyles(board.getBoardGameStyles().stream()
                        .map(bg -> bg.getGameStyle().getId())
                        .toList())
                .contents(board.getContent())
                .build();
    }

}
