package com.gamegoo.gamegoo_v2.content.report.repository;

import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRepositoryCustom {
    Page<Report> searchReports(ReportSearchRequest request, Pageable pageable);
} 