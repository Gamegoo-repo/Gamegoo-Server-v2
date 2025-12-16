package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<BoardListResponse> boards;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int totalPages;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private long totalElements;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int currentPage;

    public static BoardResponse of(Page<Board> boardPage, Map<Long, Boolean> blockedMap) {
        // 전체 페이지/개수 추출
        int totalCount = (int) boardPage.getTotalElements();
        int totalPage = (boardPage.getTotalPages() == 0) ? 1 : boardPage.getTotalPages();

        // Board -> BoardListResponse 변환
        List<BoardListResponse> boardList = boardPage.getContent().stream()
                .map(board -> {
                    Long memberId = board.getMember().getId();
                    Boolean isBlocked = blockedMap.get(memberId);
                    return BoardListResponse.of(board, isBlocked);
                })
                .collect(Collectors.toList());

        // DTO 생성
        return BoardResponse.builder()
                .totalPages(totalPage)
                .totalElements(totalCount)
                .boards(boardList)
                .currentPage(boardPage.getNumber())
                .build();
    }
}
