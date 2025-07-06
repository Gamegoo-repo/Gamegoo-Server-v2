package com.gamegoo.gamegoo_v2.controller.matching;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.controller.WithCustomMockMember;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.controller.MatchingController;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.dto.request.InitializingMatchingRequest;
import com.gamegoo.gamegoo_v2.matching.service.MatchingFacadeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchingController.class)
@DisplayName("MatchingController 제재 검증 테스트")
public class MatchingControllerBanTest extends ControllerTestSupport {

    @MockitoBean
    private MatchingFacadeService matchingFacadeService;

    private static final String API_URL_PREFIX = "/api/v2/internal/matching/priority";
    private static final Long MEMBER_ID = 1L;

    @Nested
    @DisplayName("매칭 제재 검증")
    class MatchingBanValidationTest {

        @DisplayName("제재된 사용자가 매칭을 시도하면 403 Forbidden과 제재 에러 코드를 반환한다")
        @WithCustomMockMember
        @Test
        void banned_user_cannot_start_matching() throws Exception {
            // given
            InitializingMatchingRequest request = createMatchingRequest();

            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_MATCHING))
                    .given(matchingFacadeService).calculatePriorityAndRecording(anyLong(), any(InitializingMatchingRequest.class));

            // when & then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_404"))
                    .andExpect(jsonPath("$.message").value("매칭 사용이 제한된 상태입니다."));
        }

        @DisplayName("영구 제재된 사용자는 매칭을 할 수 없다")
        @WithCustomMockMember
        @Test
        void permanently_banned_user_cannot_start_matching() throws Exception {
            // given
            InitializingMatchingRequest request = createMatchingRequest();

            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_MATCHING))
                    .given(matchingFacadeService).calculatePriorityAndRecording(anyLong(), any(InitializingMatchingRequest.class));

            // when & then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_404"))
                    .andExpect(jsonPath("$.message").value("매칭 사용이 제한된 상태입니다."));
        }

        @DisplayName("전체 제재된 사용자는 매칭을 할 수 없다")
        @WithCustomMockMember
        @Test
        void generally_banned_user_cannot_start_matching() throws Exception {
            // given
            InitializingMatchingRequest request = createMatchingRequest();

            willThrow(new MemberException(ErrorCode.MEMBER_BANNED))
                    .given(matchingFacadeService).calculatePriorityAndRecording(anyLong(), any(InitializingMatchingRequest.class));

            // when & then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_401"))
                    .andExpect(jsonPath("$.message").value("현재 계정이 제재된 상태입니다. 고객센터에 문의해주세요."));
        }

        @DisplayName("제재되지 않은 사용자는 정상적으로 매칭을 시작할 수 있다")
        @WithCustomMockMember
        @Test
        void normal_user_can_start_matching() throws Exception {
            // given
            InitializingMatchingRequest request = createMatchingRequest();

            // 정상 응답은 PriorityListResponse를 반환하므로 Mock 설정은 생략
            // (실제로는 정상적인 응답을 반환하도록 설정해야 하지만, 여기서는 예외가 발생하지 않는 것만 확인)

            // when & then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    // 헬퍼 메서드
    private InitializingMatchingRequest createMatchingRequest() {
        return InitializingMatchingRequest.builder()
                .mike(Mike.AVAILABLE)
                .mainP(Position.TOP)
                .subP(Position.JUNGLE)
                .wantP(List.of(Position.ANY))
                .matchingType(MatchingType.BASIC)
                .gameMode(GameMode.SOLO)
                .gameStyleIdList(List.of(1L, 2L))
                .build();
    }
}
