package com.gamegoo.gamegoo_v2.content.report.dto.response;

import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReportPageResponse {

    private List<ReportListResponse> reports;
    private int totalPages;
    private long totalElements;
    private int currentPage;

    public static ReportPageResponse of(Page<Report> reportPage) {
        int totalCount = (int) reportPage.getTotalElements();
        int totalPage = (reportPage.getTotalPages() == 0) ? 1 : reportPage.getTotalPages();

        List<ReportListResponse> reportList = reportPage.getContent().stream()
                .map(ReportListResponse::of)
                .collect(Collectors.toList());

        return ReportPageResponse.builder()
                .totalPages(totalPage)
                .totalElements(totalCount)
                .reports(reportList)
                .currentPage(reportPage.getNumber())
                .build();
    }
}