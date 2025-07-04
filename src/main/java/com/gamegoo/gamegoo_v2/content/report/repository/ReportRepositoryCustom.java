package com.gamegoo.gamegoo_v2.content.report.repository;

import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportSearchRequest;
import java.util.List;

public interface ReportRepositoryCustom {
    List<Report> searchReports(ReportSearchRequest request, org.springframework.data.domain.Pageable pageable);
} 