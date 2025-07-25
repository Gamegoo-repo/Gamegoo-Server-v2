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
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardUpdateResponse {

    long boardId;
    long memberId;
    Integer profileImage;
    String gameName;
    String tag;
    @Schema(ref = "#/components/schemas/Tier")
    Tier tier;
    Integer rank;
    @Schema(ref = "#/components/schemas/GameMode")
    GameMode gameMode;
    @Schema(ref = "#/components/schemas/Position")
    Position mainP;
    @Schema(ref = "#/components/schemas/Position")
    Position subP;
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    List<Position> wantP;
    @Schema(ref = "#/components/schemas/Mike")
    Mike mike;
    List<Long> gameStyles;
    String contents;

    public static BoardUpdateResponse of(Board board) {
        Member member = board.getMember();
        List<Long> gameStyleIds = board.getBoardGameStyles().stream()
                .map(bgs -> bgs.getGameStyle().getId())
                .collect(Collectors.toList());

        // 임시 멤버든 정식 멤버든 항상 Member가 존재함
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
