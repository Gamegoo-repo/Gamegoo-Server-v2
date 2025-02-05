package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyBoardListResponse {

    long boardId;
    long memberId;
    Integer profileImage;
    String gameName;
    String tag;
    Tier tier;
    int rank;
    String contents;
    LocalDateTime createdAt;

    public static MyBoardListResponse of(Board board) {
        Member member = board.getMember();
        return MyBoardListResponse.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .profileImage(board.getBoardProfileImage())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(member.getSoloTier())
                .rank(member.getSoloRank())
                .createdAt(board.getCreatedAt())
                .build();
    }

}
