package com.gamegoo.gamegoo_v2.controller.board;

import com.gamegoo.gamegoo_v2.content.board.controller.BoardController;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardBumpResponse;
import com.gamegoo.gamegoo_v2.content.board.service.BoardFacadeService;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.controller.WithCustomMockMember;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@DisplayName("BoardController 최신글 자동 끌올 테스트")
public class BoardControllerBumpLatestTest extends ControllerTestSupport {

    @MockitoBean
    private BoardFacadeService boardFacadeService;

    private static final String API_URL = "/api/v2/posts/my/latest/bump";

    @Nested
    @DisplayName("최신글 자동 끌올 성공")
    class BumpLatestBoardSuccessTest {

        @DisplayName("최신글을 성공적으로 끌올한다")
        @WithCustomMockMember
        @Test
        void bumpLatestBoard_Success() throws Exception {
            // given
            LocalDateTime bumpTime = LocalDateTime.now();
            BoardBumpResponse response = BoardBumpResponse.of(1L, bumpTime);
            given(boardFacadeService.bumpLatestBoard(any())).willReturn(response);

            // when & then
            mockMvc.perform(post(API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.boardId").value(1L))
                    .andExpect(jsonPath("$.data.bumpTime").exists());
        }
    }

    @Nested
    @DisplayName("최신글 자동 끌올 실패")
    class BumpLatestBoardFailTest {

        @DisplayName("작성한 게시글이 없으면 404 에러를 반환한다")
        @WithCustomMockMember
        @Test
        void bumpLatestBoard_BoardNotFound() throws Exception {
            // given
            willThrow(new BoardException(ErrorCode.BOARD_NOT_FOUND))
                    .given(boardFacadeService).bumpLatestBoard(any());

            // when & then
            mockMvc.perform(post(API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("BOARD_401"))
                    .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."));
        }

        @DisplayName("5분 이내에 끌올하면 400 에러를 반환한다")
        @WithCustomMockMember
        @Test
        void bumpLatestBoard_TimeLimitError() throws Exception {
            // given
            willThrow(new BoardException(ErrorCode.BUMP_TIME_LIMIT))
                    .given(boardFacadeService).bumpLatestBoard(any());

            // when & then
            mockMvc.perform(post(API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("BOARD_411"))
                    .andExpect(jsonPath("$.message").value("게시글 끌어올리기는 5분에 1회만 가능합니다."));
        }

        @DisplayName("게시글 작성이 제재된 사용자는 403 에러를 반환한다")
        @WithCustomMockMember
        @Test
        void bumpLatestBoard_BannedUser() throws Exception {
            // given
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .given(boardFacadeService).bumpLatestBoard(any());

            // when & then
            mockMvc.perform(post(API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_402"))
                    .andExpect(jsonPath("$.message").value("게시글 작성이 제한된 상태입니다."));
        }
    }
}
