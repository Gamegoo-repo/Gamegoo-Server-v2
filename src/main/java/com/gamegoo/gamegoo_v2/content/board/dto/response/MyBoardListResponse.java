package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
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
    LocalDateTime bumpTime;
    Integer mannerLevel;

    public static MyBoardListResponse of(Board board) {
        Member member = board.getMember();
        Tier tier;
        int rank;

        if (board.getGameMode() == GameMode.FREE) {
            tier = member.getFreeTier();
            rank = member.getFreeRank();
        } else {
            tier = member.getSoloTier();
            rank = member.getSoloRank();
        }

        return MyBoardListResponse.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .profileImage(member.getProfileImage())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(tier)
                .rank(rank)
                .contents(board.getContent())
                .createdAt(board.getCreatedAt())
                .bumpTime(board.getBumpTime())
                .mannerLevel(member.getMannerLevel())
                .build();
    }

}
