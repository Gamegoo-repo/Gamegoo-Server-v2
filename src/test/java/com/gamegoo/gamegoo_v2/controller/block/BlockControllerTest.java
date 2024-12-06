package com.gamegoo.gamegoo_v2.controller.block;

import com.gamegoo.gamegoo_v2.block.controller.BlockController;
import com.gamegoo.gamegoo_v2.block.service.BlockFacadeService;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.exception.BlockException;
import com.gamegoo.gamegoo_v2.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.exception.common.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlockController.class)
@Import(BlockControllerTest.TestConfig.class)
class BlockControllerTest extends ControllerTestSupport {

    @TestConfiguration
    static class TestConfig {

        @Bean
        public BlockFacadeService blockFacadeService() {
            return Mockito.mock(BlockFacadeService.class);
        }

    }

    @Autowired
    private BlockFacadeService blockFacadeService;

    private static final String API_URL_PREFIX = "/api/v2/block";

    private static final Long TARGET_MEMBER_ID = 2L;

    @Nested
    @DisplayName("회원 차단")
    class BlockMemberTest {

        @DisplayName("회원 차단 성공: 차단 성공 메시지가 반환된다.")
        @Test
        void blockMemberSucceeds() throws Exception {
            // given
            doNothing().when(blockFacadeService).blockMember(any(), eq(TARGET_MEMBER_ID));

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data").value("회원 차단 성공"));
        }

        @DisplayName("회원 차단 실패: 이미 차단한 회원인 경우 에러 응답을 반환한다.")
        @Test
        void blockMemberFailedWhenAlreadyBlocked() throws Exception {
            // given
            doThrow(new BlockException(ErrorCode.ALREADY_BLOCKED))
                    .when(blockFacadeService).blockMember(any(), eq(TARGET_MEMBER_ID));

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("이미 차단한 회원입니다."));

        }

        @DisplayName("회원 차단 실패: 대상 회원이 탈퇴한 경우 에러 응답을 반환한다.")
        @Test
        void blockMemberFailedWhenTargetIsBlind() throws Exception {
            // given
            doThrow(new MemberException(ErrorCode.TARGET_MEMBER_DEACTIVATED))
                    .when(blockFacadeService).blockMember(any(), eq(TARGET_MEMBER_ID));

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("대상 회원이 탈퇴했습니다."));
        }

    }

}

