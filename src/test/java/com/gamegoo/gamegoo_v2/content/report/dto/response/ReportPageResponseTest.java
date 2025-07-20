package com.gamegoo.gamegoo_v2.content.report.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReportPageResponse 테스트")
class ReportPageResponseTest {

    @Test
    @DisplayName("Page<Report>로부터 ReportPageResponse 생성 - 데이터가 있는 경우")
    void createFromPageWithData() {
        // given
        Member fromMember = createMember("reporter@test.com", "신고자", "KR1");
        Member toMember = createMember("reported@test.com", "피신고자", "KR2");
        
        Report report1 = Report.create(fromMember, toMember, "신고 내용1", ReportPath.CHAT, null);
        Report report2 = Report.create(fromMember, toMember, "신고 내용2", ReportPath.BOARD, null);
        
        List<Report> reports = List.of(report1, report2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(reports, pageable, 25);

        // when
        ReportPageResponse response = ReportPageResponse.of(reportPage);

        // then
        assertThat(response.getReports()).hasSize(2);
        assertThat(response.getTotalPages()).isEqualTo(3); // 25 / 10 = 3 pages
        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        
        // 첫 번째 신고 검증
        ReportListResponse firstReport = response.getReports().get(0);
        assertThat(firstReport.getFromMemberName()).isEqualTo("신고자");
        assertThat(firstReport.getFromMemberTag()).isEqualTo("KR1");
        assertThat(firstReport.getToMemberName()).isEqualTo("피신고자");
        assertThat(firstReport.getToMemberTag()).isEqualTo("KR2");
        assertThat(firstReport.getContent()).isEqualTo("신고 내용1");
        assertThat(firstReport.getPath()).isEqualTo("CHAT");
    }

    @Test
    @DisplayName("Page<Report>로부터 ReportPageResponse 생성 - 데이터가 없는 경우")
    void createFromEmptyPage() {
        // given
        List<Report> reports = List.of();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> emptyPage = new PageImpl<>(reports, pageable, 0);

        // when
        ReportPageResponse response = ReportPageResponse.of(emptyPage);

        // then
        assertThat(response.getReports()).isEmpty();
        assertThat(response.getTotalPages()).isEqualTo(1); // 빈 페이지도 최소 1페이지
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getCurrentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Page<Report>로부터 ReportPageResponse 생성 - 마지막 페이지")
    void createFromLastPage() {
        // given
        Member fromMember = createMember("reporter@test.com", "신고자", "KR1");
        Member toMember = createMember("reported@test.com", "피신고자", "KR2");
        
        Report report = Report.create(fromMember, toMember, "신고 내용", ReportPath.PROFILE, null);
        List<Report> reports = List.of(report);
        
        Pageable pageable = PageRequest.of(2, 10); // 3번째 페이지
        Page<Report> reportPage = new PageImpl<>(reports, pageable, 21); // 총 21개 데이터

        // when
        ReportPageResponse response = ReportPageResponse.of(reportPage);

        // then
        assertThat(response.getReports()).hasSize(1);
        assertThat(response.getTotalPages()).isEqualTo(3); // 21 / 10 = 3 pages
        assertThat(response.getTotalElements()).isEqualTo(21);
        assertThat(response.getCurrentPage()).isEqualTo(2); // 0-based index
    }

    @Test
    @DisplayName("Builder 패턴으로 ReportPageResponse 생성")
    void createWithBuilder() {
        // given
        ReportListResponse reportResponse = ReportListResponse.builder()
                .reportId(1L)
                .fromMemberId(10L)
                .fromMemberName("신고자")
                .fromMemberTag("KR1")
                .toMemberId(20L)
                .toMemberName("피신고자")
                .toMemberTag("KR2")
                .content("신고 내용")
                .path("CHAT")
                .build();

        // when
        ReportPageResponse response = ReportPageResponse.builder()
                .reports(List.of(reportResponse))
                .totalPages(5)
                .totalElements(50)
                .currentPage(2)
                .build();

        // then
        assertThat(response.getReports()).hasSize(1);
        assertThat(response.getTotalPages()).isEqualTo(5);
        assertThat(response.getTotalElements()).isEqualTo(50);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getReports().get(0).getReportId()).isEqualTo(1L);
    }

    private Member createMember(String email, String gameName, String tag) {
        return Member.builder()
                .email(email)
                .password("password")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag(tag)
                .soloTier(Tier.GOLD)
                .soloRank(3)
                .soloWinRate(65.5)
                .soloGameCount(100)
                .freeTier(Tier.SILVER)
                .freeRank(2)
                .freeWinRate(70.0)
                .freeGameCount(50)
                .isAgree(true)
                .build();
    }
}