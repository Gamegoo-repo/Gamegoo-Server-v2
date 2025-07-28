package com.gamegoo.gamegoo_v2.controller.report;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.content.report.controller.ReportController;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportInsertResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportListResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportPageResponse;
import com.gamegoo.gamegoo_v2.content.report.service.ReportFacadeService;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.controller.WithCustomMockMember;
import com.gamegoo.gamegoo_v2.controller.WithCustomMockAdmin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    @WithCustomMockMember
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
                    .andDo(print())
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

        @DisplayName("실패: 텍스트가 500자 초과인 경우 에러 응답을 반환한다.")
        @Test
        void addReportFailedWhenContentsSizeExceeds() throws Exception {
            // given
            List<Integer> reportCodeList = List.of(1, 2, 3);

            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("a".repeat(501))
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
                    .andExpect(jsonPath("$.message").value("contents는 500자 이내여야 합니다."));
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

    @Nested
    @DisplayName("신고 목록 조회 (관리자 전용)")
    class GetReportListTest {

        @DisplayName("실패: 인증되지 않은 사용자가 접근하면 401 에러")
        @Test
        void getReportListFailedWhenNotAuthenticated() throws Exception {
            // when // then
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @WithCustomMockMember
        @DisplayName("일반 사용자 접근 - Mock 환경에서는 빈 결과 반환")
        @Test
        void getReportListWithMemberRole() throws Exception {
            // given
            ReportPageResponse emptyResponse = ReportPageResponse.builder()
                    .reports(List.of())
                    .totalPages(0)
                    .totalElements(0)
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(emptyResponse);

            // when // then
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.reports").isEmpty())
                    .andExpect(jsonPath("$.data.totalPages").value(0))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 관리자 권한으로 신고 목록 조회 (검색 조건 없음)")
        @Test
        void getReportListAsAdminWithoutSearch() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberId(10L)
                            .fromMemberName("신고자1")
                            .fromMemberTag("KR1")
                            .toMemberId(20L)
                            .toMemberName("피신고자1")
                            .toMemberTag("KR2")
                            .content("신고 내용1")
                            .reportType("욕설/ 혐오/ 차별적 표현")
                            .path("CHAT")
                            .createdAt(java.time.LocalDateTime.now())
                            .build(),
                    ReportListResponse.builder()
                            .reportId(2L)
                            .fromMemberId(30L)
                            .fromMemberName("신고자2")
                            .fromMemberTag("KR3")
                            .toMemberId(40L)
                            .toMemberName("피신고자2")
                            .toMemberTag("KR4")
                            .content("신고 내용2")
                            .reportType("불쾌한 표현")
                            .path("BOARD")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(2)
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].reportId").value(1L))
                    .andExpect(jsonPath("$.data.reports[0].fromMemberId").value(10L))
                    .andExpect(jsonPath("$.data.reports[0].fromMemberName").value("신고자1"))
                    .andExpect(jsonPath("$.data.reports[0].fromMemberTag").value("KR1"))
                    .andExpect(jsonPath("$.data.reports[0].toMemberId").value(20L))
                    .andExpect(jsonPath("$.data.reports[0].toMemberName").value("피신고자1"))
                    .andExpect(jsonPath("$.data.reports[0].toMemberTag").value("KR2"))
                    .andExpect(jsonPath("$.data.reports[0].content").value("신고 내용1"))
                    .andExpect(jsonPath("$.data.reports[0].reportType").value("욕설/ 혐오/ 차별적 표현"))
                    .andExpect(jsonPath("$.data.reports[0].path").value("CHAT"))
                    .andExpect(jsonPath("$.data.reports[1].reportId").value(2L))
                    .andExpect(jsonPath("$.data.reports[1].fromMemberId").value(30L))
                    .andExpect(jsonPath("$.data.reports[1].fromMemberName").value("신고자2"))
                    .andExpect(jsonPath("$.data.reports[1].fromMemberTag").value("KR3"))
                    .andExpect(jsonPath("$.data.reports[1].toMemberId").value(40L))
                    .andExpect(jsonPath("$.data.reports[1].toMemberName").value("피신고자2"))
                    .andExpect(jsonPath("$.data.reports[1].toMemberTag").value("KR4"))
                    .andExpect(jsonPath("$.data.reports[1].content").value("신고 내용2"))
                    .andExpect(jsonPath("$.data.reports[1].reportType").value("불쾌한 표현"))
                    .andExpect(jsonPath("$.data.reports[1].path").value("BOARD"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 관리자 권한으로 신고 목록 조회 (검색 조건 포함)")
        @Test
        void getReportListAsAdminWithSearch() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberName("신고자1")
                            .toMemberName("피신고자1")
                            .content("신고 내용1")
                            .reportType("욕설/ 혐오/ 차별적 표현")
                            .path("CHAT")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("reportedMemberKeyword", "피신고자1")
                            .param("reporterKeyword", "신고자1")
                            .param("contentKeyword", "욕설"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].reportId").value(1L))
                    .andExpect(jsonPath("$.data.reports[0].fromMemberName").value("신고자1"))
                    .andExpect(jsonPath("$.data.reports[0].toMemberName").value("피신고자1"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 신고 사유별 필터링")
        @Test
        void getReportListWithReportTypeFilter() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberName("신고자1")
                            .toMemberName("피신고자1")
                            .content("스팸 신고")
                            .reportType("스팸 홍보/도배글")
                            .path("BOARD")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then - 신고 사유 1(스팸), 4(욕설) 필터링
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("reportTypes", "1,4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].reportType").value("스팸 홍보/도배글"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 신고 경로별 필터링")
        @Test
        void getReportListWithReportPathFilter() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberName("신고자1")
                            .toMemberName("피신고자1")
                            .content("채팅 신고")
                            .reportType("욕설/ 혐오/ 차별적 표현")
                            .path("CHAT")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then - 채팅, 프로필 경로 필터링
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("reportPaths", "CHAT,PROFILE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].path").value("CHAT"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 날짜 범위 필터링")
        @Test
        void getReportListWithDateRangeFilter() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberName("신고자1")
                            .toMemberName("피신고자1")
                            .content("최근 신고")
                            .reportType("불쾌한 표현")
                            .path("BOARD")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then - 2024년 1월 1일부터 현재까지
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("startDate", "2024-01-01T00:00:00")
                            .param("endDate", "2024-12-31T23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].content").value("최근 신고"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 신고 횟수 범위 필터링")
        @Test
        void getReportListWithReportCountFilter() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberName("신고자1")
                            .toMemberName("피신고자1")
                            .content("다중 신고 대상")
                            .reportType("욕설/ 혐오/ 차별적 표현")
                            .path("CHAT")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then - 신고 횟수 3회 이상 10회 이하
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("reportCountMin", "3")
                            .param("reportCountMax", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].content").value("다중 신고 대상"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 제재 상태별 필터링")
        @Test
        void getReportListWithBanTypeFilter() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberName("신고자1")
                            .toMemberName("피신고자1")
                            .content("제재된 사용자")
                            .reportType("욕설/ 혐오/ 차별적 표현")
                            .path("CHAT")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then - 3일, 7일 제재 상태 필터링
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("banTypes", "BAN_3D,BAN_1W"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].content").value("제재된 사용자"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 페이징 처리")
        @Test
        void getReportListWithPagination() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberName("신고자1")
                            .toMemberName("피신고자1")
                            .content("페이징 테스트")
                            .reportType("불쾌한 표현")
                            .path("BOARD")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then - 첫 번째 페이지, 사이즈 10
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].content").value("페이징 테스트"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 모든 필터 조건 조합")
        @Test
        void getReportListWithAllFilters() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberName("신고자1")
                            .toMemberName("피신고자1")
                            .content("종합 필터링 테스트")
                            .reportType("욕설/ 혐오/ 차별적 표현")
                            .path("CHAT")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then - 모든 필터 조건을 조합하여 테스트
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("reportedMemberKeyword", "피신고자")
                            .param("reporterKeyword", "신고자")
                            .param("contentKeyword", "테스트")
                            .param("reportPaths", "CHAT")
                            .param("reportTypes", "4")
                            .param("startDate", "2024-01-01T00:00:00")
                            .param("endDate", "2024-12-31T23:59:59")
                            .param("reportCountMin", "1")
                            .param("reportCountMax", "5")
                            .param("banTypes", "BAN_1D")
                            .param("isDeleted", "false")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].reportId").value(1L))
                    .andExpect(jsonPath("$.data.reports[0].fromMemberName").value("신고자1"))
                    .andExpect(jsonPath("$.data.reports[0].toMemberName").value("피신고자1"))
                    .andExpect(jsonPath("$.data.reports[0].path").value("CHAT"))
                    .andExpect(jsonPath("$.data.reports[0].content").value("종합 필터링 테스트"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 게임명#태그 조합으로 검색")
        @Test
        void getReportListWithGameNameTagSearch() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberId(10L)
                            .fromMemberName("신고자1")
                            .fromMemberTag("KR1")
                            .toMemberId(20L)
                            .toMemberName("피신고자1")
                            .toMemberTag("KR2")
                            .content("태그 검색 테스트")
                            .reportType("욕설/ 혐오/ 차별적 표현")
                            .path("CHAT")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then - gameName#tag 형식으로 검색
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("reportedMemberKeyword", "피신고자1#KR2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].toMemberName").value("피신고자1"))
                    .andExpect(jsonPath("$.data.reports[0].toMemberTag").value("KR2"))
                    .andExpect(jsonPath("$.data.reports[0].content").value("태그 검색 테스트"));
        }

        @WithCustomMockAdmin
        @DisplayName("성공: 태그만으로 검색")
        @Test
        void getReportListWithTagOnlySearch() throws Exception {
            // given
            List<ReportListResponse> responseList = List.of(
                    ReportListResponse.builder()
                            .reportId(1L)
                            .fromMemberId(10L)
                            .fromMemberName("신고자1")
                            .fromMemberTag("KR1")
                            .toMemberId(20L)
                            .toMemberName("피신고자1")
                            .toMemberTag("KR2")
                            .content("태그 단독 검색 테스트")
                            .reportType("스팸 홍보/도배글")
                            .path("BOARD")
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
            ReportPageResponse pageResponse = ReportPageResponse.builder()
                    .reports(responseList)
                    .totalPages(1)
                    .totalElements(responseList.size())
                    .currentPage(0)
                    .build();
            given(reportFacadeService.searchReports(any(), any())).willReturn(pageResponse);

            // when // then - tag만으로 검색
            mockMvc.perform(MockMvcRequestBuilders.get(API_URL_PREFIX + "/list")
                            .param("reporterKeyword", "KR1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reports").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(responseList.size()))
                    .andExpect(jsonPath("$.data.currentPage").value(0))
                    .andExpect(jsonPath("$.data.reports[0].fromMemberTag").value("KR1"))
                    .andExpect(jsonPath("$.data.reports[0].content").value("태그 단독 검색 테스트"));
        }
    }

    @Nested
    @DisplayName("제재 검증 테스트")
    class BanValidationTest {

        @DisplayName("매칭 기능 제재 검증 - 컨트롤러 레벨")
        @WithCustomMockMember
        @Test
        void testMatchingBanValidation() throws Exception {
            // given - 매칭 시도 시 제재 예외 발생
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_MATCHING))
                    .given(reportFacadeService).addReport(any(Member.class), eq(TARGET_MEMBER_ID), any(ReportRequest.class));

            List<Integer> reportCodeList = List.of(1, 2, 3);
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("신고 내용")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            // when & then - 매칭 제재 에러 응답 확인
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_404"))
                    .andExpect(jsonPath("$.message").value("매칭 사용이 제한된 상태입니다."));
        }

        @DisplayName("채팅 기능 제재 검증 - 컨트롤러 레벨")
        @WithCustomMockMember
        @Test
        void testChatBanValidation() throws Exception {
            // given - 채팅 시도 시 제재 예외 발생
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_CHATTING))
                    .given(reportFacadeService).addReport(any(Member.class), eq(TARGET_MEMBER_ID), any(ReportRequest.class));

            List<Integer> reportCodeList = List.of(1, 2, 3);
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("신고 내용")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            // when & then - 채팅 제재 에러 응답 확인
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_403"))
                    .andExpect(jsonPath("$.message").value("채팅 사용이 제한된 상태입니다."));
        }

        @DisplayName("게시글 작성 기능 제재 검증 - 컨트롤러 레벨")
        @WithCustomMockMember
        @Test
        void testPostingBanValidation() throws Exception {
            // given - 게시글 작성 시도 시 제재 예외 발생
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_POSTING))
                    .given(reportFacadeService).addReport(any(Member.class), eq(TARGET_MEMBER_ID), any(ReportRequest.class));

            List<Integer> reportCodeList = List.of(1, 2, 3);
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("신고 내용")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            // when & then - 게시글 작성 제재 에러 응답 확인
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_402"))
                    .andExpect(jsonPath("$.message").value("게시글 작성이 제한된 상태입니다."));
        }

        @DisplayName("전체 제재 검증 - 컨트롤러 레벨")
        @WithCustomMockMember
        @Test
        void testGeneralBanValidation() throws Exception {
            // given - 제재된 회원 접근 시 제재 예외 발생
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED))
                    .given(reportFacadeService).addReport(any(Member.class), eq(TARGET_MEMBER_ID), any(ReportRequest.class));

            List<Integer> reportCodeList = List.of(1, 2, 3);
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("신고 내용")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            // when & then - 전체 제재 에러 응답 확인
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_401"))
                    .andExpect(jsonPath("$.message").value("현재 계정이 제재된 상태입니다. 고객센터에 문의해주세요."));
        }

        @DisplayName("제재되지 않은 사용자는 정상적으로 기능을 사용할 수 있다")
        @WithCustomMockMember
        @Test
        void testNormalUserCanUseFeatures() throws Exception {
            // given - 정상 사용자의 신고 기능 사용
            List<Integer> reportCodeList = List.of(1, 2, 3);
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodeList)
                    .contents("신고 내용")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            ReportInsertResponse response = ReportInsertResponse.builder()
                    .reportId(1L)
                    .message("신고가 정상적으로 접수 되었습니다.")
                    .build();

            given(reportFacadeService.addReport(any(Member.class), eq(TARGET_MEMBER_ID), any(ReportRequest.class)))
                    .willReturn(response);

            // when & then - 정상적으로 처리되어야 함
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}", TARGET_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.reportId").value(1L))
                    .andExpect(jsonPath("$.data.message").value("신고가 정상적으로 접수 되었습니다."));
        }
    }

}
