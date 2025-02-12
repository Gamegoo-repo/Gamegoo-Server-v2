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
    Tier soloTier;
    Tier freeTier;
    int soloRank;
    int freeRank;
    String contents;
    LocalDateTime createdAt;
    LocalDateTime bumpTime;

    public static MyBoardListResponse of(Board board) {
        Member member = board.getMember();
        return MyBoardListResponse.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .profileImage(board.getBoardProfileImage())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .soloTier(member.getSoloTier())
                .freeTier(member.getFreeTier())
                .soloRank(member.getSoloRank())
                .freeRank(member.getFreeRank())
                .contents(board.getContent())
                .createdAt(board.getCreatedAt())
                .bumpTime(board.getBumpTime())
                .build();
    }

}
