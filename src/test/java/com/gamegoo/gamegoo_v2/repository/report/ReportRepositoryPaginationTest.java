package com.gamegoo.gamegoo_v2.repository.report;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportPath;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportSearchRequest;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ReportRepository 페이지네이션 테스트")
class ReportRepositoryPaginationTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member reporter;
    private Member reported;
    private List<Report> testReports;

    @BeforeEach
    void setUp() {
        // 테스트 멤버 생성
        reporter = createMember("reporter@test.com", "신고자", "KR1");
        reported = createMember("reported@test.com", "피신고자", "KR2");

        // 25개의 테스트 신고 데이터 생성
        testReports = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            Report report = Report.create(
                    reporter,
                    reported,
                    "신고 내용 " + i,
                    ReportPath.CHAT,
                    null
            );
            testReports.add(report);
        }
        reportRepository.saveAll(testReports);
    }

    @Test
    @DisplayName("첫 번째 페이지 조회 - 페이지 사이즈 10")
    void searchFirstPage() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder().build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Report> result = reportRepository.searchReports(request, pageable);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(0); // 현재 페이지
        assertThat(result.getSize()).isEqualTo(10); // 페이지 사이즈
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("두 번째 페이지 조회 - 페이지 사이즈 10")
    void searchSecondPage() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder().build();
        Pageable pageable = PageRequest.of(1, 10);

        // when
        Page<Report> result = reportRepository.searchReports(request, pageable);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isFalse();
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("마지막 페이지 조회 - 부분적으로 채워진 페이지")
    void searchLastPage() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder().build();
        Pageable pageable = PageRequest.of(2, 10);

        // when
        Page<Report> result = reportRepository.searchReports(request, pageable);

        // then
        assertThat(result.getContent()).hasSize(5); // 25개 중 마지막 5개
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(2);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("큰 페이지 사이즈로 조회 - 단일 페이지")
    void searchWithLargePageSize() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder().build();
        Pageable pageable = PageRequest.of(0, 100);

        // when
        Page<Report> result = reportRepository.searchReports(request, pageable);

        // then
        assertThat(result.getContent()).hasSize(25);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("범위를 벗어난 페이지 조회")
    void searchOutOfRangePage() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder().build();
        Pageable pageable = PageRequest.of(10, 10); // 10번째 페이지 (존재하지 않음)

        // when
        Page<Report> result = reportRepository.searchReports(request, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(10);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("필터링과 페이지네이션 결합 - 특정 신고자 검색")
    void searchWithFilterAndPagination() {
        // given - 신고자 키워드로 필터링
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reporterKeyword("신고자")
                .build();
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<Report> result = reportRepository.searchReports(request, pageable);

        // then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(25); // 모든 신고가 같은 신고자
        assertThat(result.getTotalPages()).isEqualTo(5); // 25 / 5 = 5 pages
        assertThat(result.getNumber()).isEqualTo(0);
        
        // 모든 결과가 필터 조건을 만족하는지 확인
        result.getContent().forEach(report -> 
            assertThat(report.getFromMember().getGameName()).contains("신고자")
        );
    }

    @Test
    @DisplayName("빈 결과에 대한 페이지네이션")
    void searchEmptyResultWithPagination() {
        // given - 존재하지 않는 키워드로 검색
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reporterKeyword("존재하지않는신고자")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Report> result = reportRepository.searchReports(request, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("Unpaged 요청 - 모든 데이터 조회")
    void searchUnpaged() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder().build();
        Pageable unpaged = Pageable.unpaged();

        // when
        Page<Report> result = reportRepository.searchReports(request, unpaged);

        // then
        assertThat(result.getContent()).hasSize(25);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isFalse();
    }

    private Member createMember(String email, String gameName, String tag) {
        Member member = Member.builder()
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
        return memberRepository.save(member);
    }
}