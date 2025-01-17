package com.gamegoo.gamegoo_v2.controller.report;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.content.report.controller.ReportController;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportInsertResponse;
import com.gamegoo.gamegoo_v2.content.report.service.ReportFacadeService;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
public class ReportControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ReportFacadeService reportFacadeService;

    private static final String API_URL_PREFIX = "/api/v2/report";
    private static final Long TARGET_MEMBER_ID = 2L;

    @Nested
    @DisplayName("신고 등록")
    class AddReportTest {

        @DisplayName("실패: 신고 코드 리스트가 빈 리스트인 경우 에러 응답을 반환한다.")
        @Test
        void addReportFailedWhenReportCodeListIsEmpty() throws Exception {
            // given
            List<Integer> reportCodeList = List.of();

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("신고 코드 리스트는 비워둘 수 없습니다."));
        }

        @DisplayName("실패: 신고 코드 리스트에 중복된 값이 있는 경우 에러 응답을 반환한다.")
        @Test
        void addReportFailedWhenReportCodeListIsDuplicated() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(1, 1);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("중복된 값을 포함할 수 없습니다."));
        }

        @DisplayName("실패: 신고 코드 리스트에 1보다 작은 값이 있는 경우 에러 응답을 반환한다.")
        @Test
        void addReportFailedWhenReportCodeListLessThanMin() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(0, 1, 2);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("report code는 1 이상의 값이어야 합니다."));
        }

        @DisplayName("실패: 신고 코드 리스트에 6보다 큰 값이 있는 경우 에러 응답을 반환한다.")
        @Test
        void addReportFailedWhenReportCodeListGreaterThanMax() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(5, 6, 7);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("report code는 6 이하의 값이어야 합니다."));
        }

        @DisplayName("실패: 텍스트가 1000자 초과인 경우 에러 응답을 반환한다.")
        @Test
        void addReportFailedWhenContentsSizeExceeds() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(1, 2, 3);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("a".repeat(1001))
                    .pathCode(1)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("contents는 1000자 이내여야 합니다."));
        }

        @DisplayName("실패: 경로 코드 값이 없는 경우 에러 응답을 반환한다.")
        @Test
        void addReportFailedWhenPathCodeIsNull() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(1, 2, 3);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("contents")
                    .pathCode(null)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("path code는 필수 값 입니다."));

        }

        @DisplayName("실패: 경로 코드가 1보다 작은 경우 에러 응답을 반환한다.")
        @Test
        void addReportFailedWhenPathCodeLessThanMin() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(1, 2, 3);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("contents")
                    .pathCode(0)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("path code는 1 이상의 값이어야 합니다."));

        }

        @DisplayName("실패: 경로 코드가 3보다 큰 경우 에러 응답을 반환한다.")
        @Test
        void addReportFailedWhenPathCodeGreaterThanMax() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(1, 2, 3);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("contents")
                    .pathCode(4)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("path code는 3 이하의 값이어야 합니다."));
        }

        @DisplayName("성공")
        @Test
        void addReportFailedSucceeds() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(1, 2, 3);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.reportId").value(1L))
                    .andExpect(jsonPath("$.data.message").value("신고가 정상적으로 접수 되었습니다."));
        }

        @DisplayName("텍스트, 게시글 id가 null일 때 성공")
        @Test
        void addReportFailedSucceedsWhenNull() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(1, 2, 3);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents(null)
                    .pathCode(1)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID),
                    any(ReportRequest.class))).willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.reportId").value(1L))
                    .andExpect(jsonPath("$.data.message").value("신고가 정상적으로 접수 되었습니다."));
        }

    }

}
