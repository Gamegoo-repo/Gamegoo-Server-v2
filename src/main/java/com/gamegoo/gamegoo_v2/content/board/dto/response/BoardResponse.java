package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardResponse {
    private Long id;
    private Long memberId;
    private String nickname;
    private Tier tier;
    private GameMode gameMode;
    private Position position1;
    private Position position2;
    private Mike mike;
    private LocalDateTime createdAt;
    private LocalDateTime bumpTime;
    private List<BoardListResponse> boards;
    private int totalPages;
    private long totalElements;
    private int currentPage;

    public static BoardResponse of(Page<Board> boardPage) {
        // 전체 페이지/개수 추출
        int totalCount = (int) boardPage.getTotalElements();
        int totalPage = (boardPage.getTotalPages() == 0) ? 1 : boardPage.getTotalPages();

        // Board -> BoardListResponse 변환
        List<BoardListResponse> boardList = boardPage.getContent().stream()
                .map(BoardListResponse::of)
                .collect(Collectors.toList());

        // DTO 생성
        return BoardResponse.builder()
                .totalPages(totalPage)
                .totalElements(totalCount)
                .boards(boardList)
                .currentPage(boardPage.getNumber())
                .build();
    }

    public static BoardResponse of(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .memberId(board.getMember().getId())
                .nickname(board.getMember().getGameName())
                .tier(board.getMember().getSoloTier())
                .gameMode(board.getGameMode())
                .position1(board.getMainP())
                .position2(board.getSubP())
                .mike(board.getMike())
                .createdAt(board.getCreatedAt())
                .bumpTime(board.getBumpTime())
                .build();
    }
}
