package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardResponse {

    Integer totalPage;
    Integer totalCount;
    List<BoardListResponse> boards;

    public static BoardResponse of(Page<Board> boardPage) {
        // 전체 페이지/개수 추출
        int totalCount = (int) boardPage.getTotalElements();
        int totalPage = (boardPage.getTotalPages() == 0) ? 1 : boardPage.getTotalPages();

        // Board -> BoardListResponse 변환
        List<BoardListResponse> boardList = boardPage.getContent().stream()
                .map(board -> {
                    // Member의 MemberChampion 목록을 기반으로 ChampionStatsResponse 리스트 생성
                    Member member = board.getMember();
                    List<ChampionStatsResponse> championStatsResponseList = member.getMemberChampionList() != null
                            ? member.getMemberChampionList().stream()
                            .map(mc -> ChampionStatsResponse.builder()
                                    .championId(mc.getChampion().getId())
                                    .championName(mc.getChampion().getName())
                                    .wins(mc.getWins())
                                    .games(mc.getGames())
                                    .winRate(mc.getGames() > 0 ? (double) mc.getWins() / mc.getGames() : 0)
                                    // 필요 시 csPerMinute 값도 포함 (여기서는 그대로 사용)
                                    .csPerMinute(mc.getCsPerMinute())
                                    .build())
                            .collect(Collectors.toList())
                            : List.of();
                    return BoardListResponse.of(board, championStatsResponseList);
                })
                .collect(Collectors.toList());

        // DTO 생성
        return BoardResponse.builder()
                .totalPage(totalPage)
                .totalCount(totalCount)
                .boards(boardList)
                .build();
    }

}
