package com.gamegoo.gamegoo_v2.content.report.repository;

import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.domain.QReport;
import com.gamegoo.gamegoo_v2.content.report.domain.QReportTypeMapping;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportSortOrder;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportSearchRequest;
import com.gamegoo.gamegoo_v2.content.board.domain.QBoard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public org.springframework.data.domain.Page<Report> searchReports(ReportSearchRequest request, org.springframework.data.domain.Pageable pageable) {
        QReport report = QReport.report;
        QReportTypeMapping reportTypeMapping = QReportTypeMapping.reportTypeMapping;
        QBoard board = QBoard.board;

        BooleanBuilder builder = new BooleanBuilder();

        // 1. 신고자 키워드 검색 (gameName, tag, gameName#tag 조합으로 검색)
        if (request.getReporterKeyword() != null && !request.getReporterKeyword().isEmpty()) {
            builder.and(
                report.fromMember.gameName.contains(request.getReporterKeyword())
                .or(report.fromMember.tag.contains(request.getReporterKeyword()))
                .or(report.fromMember.gameName.concat("#").concat(report.fromMember.tag).contains(request.getReporterKeyword()))
            );
        }

        // 2. 피신고자 키워드 검색 (gameName, tag, gameName#tag 조합으로 검색)
        if (request.getReportedMemberKeyword() != null && !request.getReportedMemberKeyword().isEmpty()) {
            builder.and(
                report.toMember.gameName.contains(request.getReportedMemberKeyword())
                .or(report.toMember.tag.contains(request.getReportedMemberKeyword()))
                .or(report.toMember.gameName.concat("#").concat(report.toMember.tag).contains(request.getReportedMemberKeyword()))
            );
        }

        // 3. 상세 내용 키워드 검색
        if (request.getContentKeyword() != null && !request.getContentKeyword().isEmpty()) {
            builder.and(report.content.contains(request.getContentKeyword()));
        }

        // 4. 신고된 페이지 유형 다중선택
        if (request.getReportPaths() != null && !request.getReportPaths().isEmpty()) {
            builder.and(report.path.in(request.getReportPaths()));
        }

        // 5. 신고 사유 다중선택 (ReportTypeMapping과 join 필요)
        if (request.getReportTypes() != null && !request.getReportTypes().isEmpty()) {
            builder.and(reportTypeMapping.code.in(request.getReportTypes()));
        }

        // 6. 날짜 범위 필터
        if (request.getStartDate() != null && request.getEndDate() != null) {
            builder.and(report.createdAt.between(request.getStartDate(), request.getEndDate()));
        } else if (request.getStartDate() != null) {
            builder.and(report.createdAt.goe(request.getStartDate()));
        } else if (request.getEndDate() != null) {
            builder.and(report.createdAt.loe(request.getEndDate()));
        }

        // 7. 신고 누적 횟수 구간 검색
        if (request.getReportCountMin() != null && request.getReportCountMax() != null) {
            // 서브쿼리로 신고 대상별 신고 횟수 집계 후 조건 추가
            builder.and(report.toMember.id.in(
                queryFactory.select(report.toMember.id)
                    .from(report)
                    .groupBy(report.toMember.id)
                    .having(report.count().between(request.getReportCountMin(), request.getReportCountMax()))
            ));
        } else if (request.getReportCountMin() != null) {
            builder.and(report.toMember.id.in(
                queryFactory.select(report.toMember.id)
                    .from(report)
                    .groupBy(report.toMember.id)
                    .having(report.count().goe(request.getReportCountMin()))
            ));
        } else if (request.getReportCountMax() != null) {
            builder.and(report.toMember.id.in(
                queryFactory.select(report.toMember.id)
                    .from(report)
                    .groupBy(report.toMember.id)
                    .having(report.count().loe(request.getReportCountMax()))
            ));
        } else if (request.getReportCountExact() != null) {
            builder.and(report.toMember.id.in(
                queryFactory.select(report.toMember.id)
                    .from(report)
                    .groupBy(report.toMember.id)
                    .having(report.count().eq(request.getReportCountExact().longValue()))
            ));
        }

        // 8. 삭제된 게시글 필터
        if (request.getIsDeleted() != null) {
            if (request.getIsDeleted()) {
                // 삭제된 게시글만 조회
                builder.and(report.sourceBoard.isNotNull().and(report.sourceBoard.deleted.eq(true)));
            } else {
                // 삭제되지 않은 게시글만 조회
                builder.and(report.sourceBoard.isNull().or(report.sourceBoard.deleted.eq(false)));
            }
        }

        // 9. 계정 제재 상태 필터 (Member 엔티티에 banType 필드가 있다고 가정)
        if (request.getBanTypes() != null && !request.getBanTypes().isEmpty()) {
            builder.and(report.toMember.banType.in(request.getBanTypes()));
        }

        // 전체 개수 조회
        long totalCount = queryFactory.selectFrom(report)
                .leftJoin(reportTypeMapping).on(reportTypeMapping.report.eq(report))
                .leftJoin(report.sourceBoard, board)
                .leftJoin(report.toMember)
                .leftJoin(report.fromMember)
                .where(builder)
                .distinct()
                .fetchCount();

        // 데이터 조회
        var query = queryFactory.selectFrom(report)
                .leftJoin(reportTypeMapping).on(reportTypeMapping.report.eq(report))
                .leftJoin(report.sourceBoard, board)
                .leftJoin(report.toMember)
                .leftJoin(report.fromMember)
                .where(builder)
                .distinct();

        // 정렬 처리
        if (request.getSortOrder() == ReportSortOrder.OLDEST) {
            query = query.orderBy(report.createdAt.asc());
        } else {
            // 기본값: 최신순 (LATEST 또는 null)
            query = query.orderBy(report.createdAt.desc());
        }

        // 페이징 처리
        if (pageable != null && pageable.isPaged()) {
            query = query.offset(pageable.getOffset())
                        .limit(pageable.getPageSize());
        }

        var content = new java.util.ArrayList<>(query.fetch());
        
        return new PageImpl<>(content, pageable, totalCount);
    }
}
