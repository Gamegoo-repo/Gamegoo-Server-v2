package com.gamegoo.gamegoo_v2.controller.block;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.controller.WithCustomMockMember;
import com.gamegoo.gamegoo_v2.core.exception.BlockException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.block.controller.BlockController;
import com.gamegoo.gamegoo_v2.social.block.dto.BlockListResponse;
import com.gamegoo.gamegoo_v2.social.block.dto.BlockResponse;
import com.gamegoo.gamegoo_v2.social.block.service.BlockFacadeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlockController.class)
@WithCustomMockMember
class BlockControllerTest extends ControllerTestSupport {

    @MockitoBean
    private BlockFacadeService blockFacadeService;

    private static final String API_URL_PREFIX = "/api/v2/block";
    private static final Long TARGET_MEMBER_ID = 2L;

    @Nested
    @DisplayName("회원 차단")
    class BlockMemberTest {

        @DisplayName("회원 차단 성공")
        @Test
        void blockMemberSucceeds() throws Exception {
            // given
            BlockResponse response = BlockResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .message("회원 차단 성공")
                    .build();

            given(blockFacadeService.blockMember(any(Member.class), eq(TARGET_MEMBER_ID))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.targetMemberId").value(TARGET_MEMBER_ID))
                    .andExpect(jsonPath("$.data.message").value("회원 차단 성공"));
        }

        @DisplayName("회원 차단 실패: 이미 차단한 회원인 경우 에러 응답을 반환한다.")
        @Test
        void blockMemberFailedWhenAlreadyBlocked() throws Exception {
            // given
            willThrow(new BlockException(ErrorCode.ALREADY_BLOCKED))
                    .given(blockFacadeService).blockMember(any(Member.class), eq(TARGET_MEMBER_ID));

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_BLOCKED.getMessage()));

        }

        @DisplayName("회원 차단 실패: 대상 회원이 탈퇴한 경우 에러 응답을 반환한다.")
        @Test
        void blockMemberFailedWhenTargetIsBlind() throws Exception {
            // given
            willThrow(new BlockException(ErrorCode.TARGET_MEMBER_DEACTIVATED))
                    .given(blockFacadeService).blockMember(any(Member.class), eq(TARGET_MEMBER_ID));

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage()));
        }

    }

    @Nested
    @DisplayName("차단한 회원 목록 조회")
    class GetBlockListTest {

        @DisplayName("차단한 회원 목록 조회 성공: 조회 결과가 반환된다.")
        @Test
        void getBlockListSucceeds() throws Exception {
            // given
            BlockListResponse response = BlockListResponse.builder()
                    .blockedMemberList(new ArrayList<>())
                    .listSize(0)
                    .totalPage(0)
                    .totalElements(0)
                    .isFirst(true)
                    .isLast(true)
                    .build();

            given(blockFacadeService.getBlockList(any(Member.class), any(Integer.class))).willReturn(response);

            // when // then
            int pageIdx = 1;
            mockMvc.perform(get(API_URL_PREFIX)
                            .param("page", String.valueOf(pageIdx)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.blockedMemberList").isEmpty())
                    .andExpect(jsonPath("$.data.listSize").value(0))
                    .andExpect(jsonPath("$.data.totalPage").value(0))
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.isFirst").value(true))
                    .andExpect(jsonPath("$.data.isLast").value(true));
        }

        @DisplayName("차단한 회원 목록 조회 실패: 페이지 번호가 1 미만인 경우 에러 응답을 반환한다")
        @Test
        void getBlockListFailedWhenPageIsNotValid() throws Exception {
            // when // then
            int pageIdx = 0;
            mockMvc.perform(get(API_URL_PREFIX)
                            .param("page", String.valueOf(pageIdx)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("페이지 번호는 1 이상의 값이어야 합니다."));
        }

    }

    @Nested
    @DisplayName("회원 차단 해제")
    class UnblockMemberTest {

        @DisplayName("회원 차단 해제 성공: 차단 해제 성공 메시지가 반환된다.")
        @Test
        void unblockMemberSucceeds() throws Exception {
            // given
            BlockResponse response = BlockResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .message("회원 차단 해제 성공")
                    .build();

            given(blockFacadeService.unBlockMember(any(Member.class), eq(TARGET_MEMBER_ID))).willReturn(response);

            // when // then
            mockMvc.perform(delete(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.targetMemberId").value(TARGET_MEMBER_ID))
                    .andExpect(jsonPath("$.data.message").value("회원 차단 해제 성공"));
        }

        @DisplayName("회원 차단 해제 실패: 대상 회원을 차단한 상태가 아닌 경우 에러 응답을 반환한다.")
        @Test
        void unblockMemberFailedWhenTargetMemberIsNotBlocked() throws Exception {
            // given
            willThrow(new BlockException(ErrorCode.TARGET_MEMBER_NOT_BLOCKED))
                    .given(blockFacadeService).unBlockMember(any(Member.class), eq(TARGET_MEMBER_ID));

            // when // then
            mockMvc.perform(delete(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ErrorCode.TARGET_MEMBER_NOT_BLOCKED.getMessage()));

        }

        @DisplayName("회원 차단 해제 실패: 대상 회원이 탈퇴한 경우 에러 응답을 반환한다.")
        @Test
        void unblockMemberFailedWhenTargetIsBlind() throws Exception {
            // given
            willThrow(new BlockException(ErrorCode.TARGET_MEMBER_DEACTIVATED))
                    .given(blockFacadeService).unBlockMember(any(Member.class), eq(TARGET_MEMBER_ID));

            // when // then
            mockMvc.perform(delete(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage()));
        }

    }

    @Nested
    @DisplayName("차단 목록에서 삭제")
    class DeleteBlockTest {

        @DisplayName("차단 목록에서 삭제 성공: 차단 목록에서 삭제 성공 메시지가 반환된다.")
        @Test
        void deleteBlockSucceeds() throws Exception {
            // given
            BlockResponse response = BlockResponse.builder()
                    .targetMemberId(TARGET_MEMBER_ID)
                    .message("차단 목록에서 삭제 성공")
                    .build();

            given(blockFacadeService.deleteBlock(any(Member.class), eq(TARGET_MEMBER_ID))).willReturn(response);

            // when // then
            mockMvc.perform(delete(API_URL_PREFIX + "/delete/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.targetMemberId").value(TARGET_MEMBER_ID))
                    .andExpect(jsonPath("$.data.message").value("차단 목록에서 삭제 성공"));
        }

        @DisplayName("차단 목록에서 삭제 실패: 대상 회원을 차단한 상태가 아닌 경우 에러 응답을 반환한다.")
        @Test
        void deleteBlockFailedWhenTargetMemberIsNotBlocked() throws Exception {
            // given
            willThrow(new BlockException(ErrorCode.TARGET_MEMBER_NOT_BLOCKED))
                    .given(blockFacadeService).deleteBlock(any(Member.class), eq(TARGET_MEMBER_ID));

            // when // then
            mockMvc.perform(delete(API_URL_PREFIX + "/delete/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ErrorCode.TARGET_MEMBER_NOT_BLOCKED.getMessage()));

        }

        @DisplayName("차단 목록에서 삭제 실패: 대상 회원이 탈퇴하지 않은 경우 에러 응답을 반환한다.")
        @Test
        void deleteBlockFailedWhenTargetIsBlind() throws Exception {
            // given
            willThrow(new BlockException(ErrorCode.DELETE_BLOCKED_MEMBER_FAILED))
                    .given(blockFacadeService).deleteBlock(any(Member.class), eq(TARGET_MEMBER_ID));

            // when // then
            mockMvc.perform(delete(API_URL_PREFIX + "/delete/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(ErrorCode.DELETE_BLOCKED_MEMBER_FAILED.getMessage()));
        }

    }

}

