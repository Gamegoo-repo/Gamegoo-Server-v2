package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BoardInsertResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private long boardId;
    private Long memberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer profileImage;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String gameName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String tag;
    @Schema(ref = "#/components/schemas/Tier", requiredMode = Schema.RequiredMode.REQUIRED)
    private Tier tier;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int rank;
    @Schema(ref = "#/components/schemas/GameMode", requiredMode = Schema.RequiredMode.REQUIRED)
    private GameMode gameMode;
    @Schema(ref = "#/components/schemas/Position", requiredMode = Schema.RequiredMode.REQUIRED)
    private Position mainP;
    @Schema(ref = "#/components/schemas/Position", requiredMode = Schema.RequiredMode.REQUIRED)
    private Position subP;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    private List<Position> wantP;
    @Schema(ref = "#/components/schemas/Mike", requiredMode = Schema.RequiredMode.REQUIRED)
    private Mike mike;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
                .gameStyles(board.getBoardGameStyles().stream()
                        .map(bg -> bg.getGameStyle().getId())
                        .toList())
                .contents(board.getContent())
                .build();
    }

    public static BoardInsertResponse ofGuest(Board board) {
        Member tmpMember = board.getMember();
        
        Tier tier;
        int rank;
        if (board.getGameMode() == GameMode.FREE) {
            tier = tmpMember.getFreeTier();
            rank = tmpMember.getFreeRank();
        } else {
            tier = tmpMember.getSoloTier();
            rank = tmpMember.getSoloRank();
        }
        
        return BoardInsertResponse.builder()
                .boardId(board.getId())
                .memberId(null) // 게스트는 memberId null
                .profileImage(board.getBoardProfileImage())
                .gameName(tmpMember.getGameName())
                .tag(tmpMember.getTag())
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
