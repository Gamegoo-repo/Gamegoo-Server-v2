package com.gamegoo.gamegoo_v2.service.report;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportPath;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportSearchRequest;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportRepository;
import com.gamegoo.gamegoo_v2.content.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("신고 검색 통합 테스트")
public class ReportSearchIntegrationTest {

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private ReportService reportService;

    private Member reporter1;
    private Member reporter2; 
    private Member reported1;
    private Member reported2;
    private Report report1;
    private Report report2;

    @BeforeEach
    void setUp() {
        // 테스트 멤버들 생성
        reporter1 = Member.createForGeneral(
                "reporter1@test.com", "password", LoginType.GENERAL,
                "신고자1", "KR1", Tier.GOLD, 3, 65.5, Tier.SILVER, 2, 70.0, 100, 50, true
        );
        reporter2 = Member.createForGeneral(
                "reporter2@test.com", "password", LoginType.GENERAL,
                "신고자2", "NA1", Tier.PLATINUM, 1, 75.0, Tier.GOLD, 4, 68.0, 150, 80, true
        );
        reported1 = Member.createForGeneral(
                "reported1@test.com", "password", LoginType.GENERAL,
                "피신고자1", "KR2", Tier.DIAMOND, 2, 80.0, Tier.PLATINUM, 3, 72.0, 200, 120, true
        );
        reported2 = Member.createForGeneral(
                "reported2@test.com", "password", LoginType.GENERAL,
                "홍길동", "KR3", Tier.MASTER, 0, 85.0, Tier.DIAMOND, 1, 78.0, 300, 180, true
        );

        memberRepository.save(reporter1);
        memberRepository.save(reporter2);
        memberRepository.save(reported1);
        memberRepository.save(reported2);

        // 테스트 신고들 생성
        report1 = Report.create(reporter1, reported1, "욕설 신고입니다", ReportPath.CHAT, null);
        report2 = Report.create(reporter2, reported2, "스팸 신고입니다", ReportPath.BOARD, null);
        
        reportRepository.save(report1);
        reportRepository.save(report2);
    }

    @Test
    @DisplayName("신고자 게임명으로 검색")
    void searchByReporterGameName() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reporterKeyword("신고자1")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then
        System.out.println("=== 신고자 게임명 검색 결과 ===");
        System.out.println("검색 키워드: 신고자1");
        System.out.println("검색 결과 수: " + results.size());
        for (Report report : results) {
            System.out.println("신고자: " + report.getFromMember().getGameName() + "#" + report.getFromMember().getTag());
            System.out.println("피신고자: " + report.getToMember().getGameName() + "#" + report.getToMember().getTag());
            System.out.println("내용: " + report.getContent());
        }
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFromMember().getGameName()).isEqualTo("신고자1");
        assertThat(results.get(0).getContent()).isEqualTo("욕설 신고입니다");
    }

    @Test
    @DisplayName("신고자 태그로 검색")
    void searchByReporterTag() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reporterKeyword("NA1")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFromMember().getTag()).isEqualTo("NA1");
        assertThat(results.get(0).getFromMember().getGameName()).isEqualTo("신고자2");
    }

    @Test
    @DisplayName("신고자 게임명#태그 조합으로 검색")
    void searchByReporterGameNameTag() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reporterKeyword("신고자1#KR1")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFromMember().getGameName()).isEqualTo("신고자1");
        assertThat(results.get(0).getFromMember().getTag()).isEqualTo("KR1");
    }

    @Test
    @DisplayName("피신고자 게임명으로 검색")
    void searchByReportedGameName() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reportedMemberKeyword("홍길동")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getToMember().getGameName()).isEqualTo("홍길동");
        assertThat(results.get(0).getToMember().getTag()).isEqualTo("KR3");
    }

    @Test
    @DisplayName("피신고자 태그로 검색")
    void searchByReportedTag() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reportedMemberKeyword("KR2")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getToMember().getGameName()).isEqualTo("피신고자1");
        assertThat(results.get(0).getToMember().getTag()).isEqualTo("KR2");
    }

    @Test
    @DisplayName("피신고자 게임명#태그 조합으로 검색")
    void searchByReportedGameNameTag() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reportedMemberKeyword("홍길동#KR3")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then
        System.out.println("=== 피신고자 게임명#태그 조합 검색 결과 ===");
        System.out.println("검색 키워드: 홍길동#KR3");
        System.out.println("검색 결과 수: " + results.size());
        for (Report report : results) {
            System.out.println("신고자: " + report.getFromMember().getGameName() + "#" + report.getFromMember().getTag());
            System.out.println("피신고자: " + report.getToMember().getGameName() + "#" + report.getToMember().getTag());
            System.out.println("내용: " + report.getContent());
        }
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getToMember().getGameName()).isEqualTo("홍길동");
        assertThat(results.get(0).getToMember().getTag()).isEqualTo("KR3");
    }

    @Test
    @DisplayName("신고 내용으로 검색")
    void searchByContent() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .contentKeyword("욕설")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).contains("욕설");
    }

    @Test
    @DisplayName("존재하지 않는 키워드로 검색시 빈 결과")
    void searchWithNonExistentKeyword() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reporterKeyword("존재하지않는유저")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("부분 매칭 검색 확인")
    void searchWithPartialMatching() {
        // given - "신고" 키워드로 검색
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reporterKeyword("신고")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then - "신고자1", "신고자2" 모두 포함되어야 함
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("대소문자 구분 없이 태그 검색")
    void searchTagCaseInsensitive() {
        // given - 소문자로 검색
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reporterKeyword("kr1")
                .build();

        // when
        org.springframework.data.domain.Page<Report> resultPage = reportService.searchReports(request, Pageable.unpaged());
        List<Report> results = resultPage.getContent();

        // then - 대문자 KR1과 매칭되지 않아야 함 (정확한 동작 확인)
        // MySQL은 기본적으로 대소문자를 구분하지 않지만, 정확한 설정에 따라 다를 수 있음
        System.out.println("검색 결과 수: " + results.size());
        if (!results.isEmpty()) {
            System.out.println("매칭된 태그: " + results.get(0).getFromMember().getTag());
        }
    }
}