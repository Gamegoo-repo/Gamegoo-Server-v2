package com.gamegoo.gamegoo_v2.controller.board;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.content.board.controller.BoardController;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.response.MyBoardResponse;
import com.gamegoo.gamegoo_v2.content.board.service.BoardFacadeService;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.controller.WithCustomMockMember;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@DisplayName("BoardController 제재 검증 테스트")
public class BoardControllerBanTest extends ControllerTestSupport {

    @MockitoBean
    private BoardFacadeService boardFacadeService;

    private static final String API_URL_PREFIX = "/api/v2/posts";
    private static final Long BOARD_ID = 1L;

    @Nested
    @DisplayName("게시글 작성 제재 검증")
    class BoardCreateBanValidationTest {

        @DisplayName("제재된 사용자가 게시글을 작성하려고 하면 403 Forbidden과 제재 에러 코드를 반환한다")
        @WithCustomMockMember
        @Test
        void banned_user_cannot_create_board() throws Exception {
            // given
            BoardInsertRequest request = createBoardInsertRequest();

            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .given(boardFacadeService).createBoard(any(BoardInsertRequest.class), any());

            // when & then
            mockMvc.perform(post(API_URL_PREFIX)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_402"))
                    .andExpect(jsonPath("$.message").value("게시글 작성이 제한된 상태입니다."));
        }

        @DisplayName("영구 제재된 사용자는 게시글을 작성할 수 없다")
        @WithCustomMockMember
        @Test
        void permanently_banned_user_cannot_create_board() throws Exception {
            // given
            BoardInsertRequest request = createBoardInsertRequest();

            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .given(boardFacadeService).createBoard(any(BoardInsertRequest.class), any());

            // when & then
            mockMvc.perform(post(API_URL_PREFIX)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_402"))
                    .andExpect(jsonPath("$.message").value("게시글 작성이 제한된 상태입니다."));
        }

        @DisplayName("제재되지 않은 사용자는 정상적으로 게시글을 작성할 수 있다")
        @WithCustomMockMember
        @Test
        void normal_user_can_create_board() throws Exception {
            // given
            BoardInsertRequest request = createBoardInsertRequest();

            // when & then
            mockMvc.perform(post(API_URL_PREFIX)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("게시글 수정 제재 검증")
    class BoardUpdateBanValidationTest {

        @DisplayName("제재된 사용자가 게시글을 수정하려고 하면 403 Forbidden과 제재 에러 코드를 반환한다")
        @WithCustomMockMember
        @Test
        void banned_user_cannot_update_board() throws Exception {
            // given
            BoardUpdateRequest request = createBoardUpdateRequest();

            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .given(boardFacadeService).updateBoard(any(BoardUpdateRequest.class), any(), anyLong());

            // when & then
            mockMvc.perform(put(API_URL_PREFIX + "/{boardId}", BOARD_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_402"))
                    .andExpect(jsonPath("$.message").value("게시글 작성이 제한된 상태입니다."));
        }

        @DisplayName("제재되지 않은 사용자는 정상적으로 게시글을 수정할 수 있다")
        @WithCustomMockMember
        @Test
        void normal_user_can_update_board() throws Exception {
            // given
            BoardUpdateRequest request = createBoardUpdateRequest();

            // when & then
            mockMvc.perform(put(API_URL_PREFIX + "/{boardId}", BOARD_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("게시글 끌올 제재 검증")
    class BoardBumpBanValidationTest {

        @DisplayName("제재된 사용자가 게시글을 끌올하려고 하면 403 Forbidden과 제재 에러 코드를 반환한다")
        @WithCustomMockMember
        @Test
        void banned_user_cannot_bump_board() throws Exception {
            // given
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .given(boardFacadeService).bumpBoard(anyLong(), any());

            // when & then
            mockMvc.perform(post(API_URL_PREFIX + "/{boardId}/bump", BOARD_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_402"))
                    .andExpect(jsonPath("$.message").value("게시글 작성이 제한된 상태입니다."));
        }

        @DisplayName("제재되지 않은 사용자는 정상적으로 게시글을 끌올할 수 있다")
        @WithCustomMockMember
        @Test
        void normal_user_can_bump_board() throws Exception {
            // when & then
            mockMvc.perform(post(API_URL_PREFIX + "/{boardId}/bump", BOARD_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("게시글 관련 기능 제재 검증")
    class BoardFeatureBanValidationTest {

        @DisplayName("제재된 사용자가 게시글 목록을 조회하려고 하면 정상적으로 처리된다 (읽기는 허용)")
        @WithCustomMockMember
        @Test
        void banned_user_can_view_board_list() throws Exception {
            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/cursor"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @DisplayName("제재된 사용자가 게시글 상세를 조회하려고 하면 정상적으로 처리된다 (읽기는 허용)")
        @WithCustomMockMember
        @Test
        void banned_user_can_view_board_detail() throws Exception {
            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/list/{boardId}", BOARD_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @DisplayName("제재된 사용자가 자신의 게시글 목록을 조회하려고 하면 정상적으로 처리된다 (읽기는 허용)")
        @WithCustomMockMember
        @Test
        void banned_user_can_view_own_board_list() throws Exception {
            // given
            MyBoardResponse mockResponse = MyBoardResponse.builder()
                    .totalPage(1)
                    .totalCount(0)
                    .myBoards(List.of())
                    .build();
            given(boardFacadeService.getMyBoardList(any(), anyInt()))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/my?page=1"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @DisplayName("제재된 사용자가 게시글을 삭제하려고 하면 정상적으로 처리된다 (삭제는 허용)")
        @WithCustomMockMember
        @Test
        void banned_user_can_delete_board() throws Exception {
            // when & then
            mockMvc.perform(delete(API_URL_PREFIX + "/{boardId}", BOARD_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    // 헬퍼 메서드
    private BoardInsertRequest createBoardInsertRequest() {
        BoardInsertRequest request = new BoardInsertRequest();
        ReflectionTestUtils.setField(request, "gameMode", GameMode.SOLO);
        ReflectionTestUtils.setField(request, "mainP", Position.TOP);
        ReflectionTestUtils.setField(request, "subP", Position.JUNGLE);
        ReflectionTestUtils.setField(request, "wantP", List.of(Position.ANY));
        ReflectionTestUtils.setField(request, "mike", Mike.AVAILABLE);
        ReflectionTestUtils.setField(request, "gameStyles", List.of(1L, 2L));
        ReflectionTestUtils.setField(request, "contents", "테스트 게시글 내용");
        return request;
    }

    private BoardUpdateRequest createBoardUpdateRequest() {
        BoardUpdateRequest request = new BoardUpdateRequest();
        ReflectionTestUtils.setField(request, "gameMode", GameMode.SOLO);
        ReflectionTestUtils.setField(request, "mainP", Position.TOP);
        ReflectionTestUtils.setField(request, "subP", Position.JUNGLE);
        ReflectionTestUtils.setField(request, "wantP", List.of(Position.ANY));
        ReflectionTestUtils.setField(request, "mike", Mike.AVAILABLE);
        ReflectionTestUtils.setField(request, "gameStyles", List.of(1L, 2L));
        ReflectionTestUtils.setField(request, "contents", "수정된 게시글 내용");
        return request;
    }
}
