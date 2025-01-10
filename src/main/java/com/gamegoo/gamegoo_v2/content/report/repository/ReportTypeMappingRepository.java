package com.gamegoo.gamegoo_v2.content.report.repository;

import com.gamegoo.gamegoo_v2.content.report.domain.ReportTypeMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportTypeMappingRepository extends JpaRepository<ReportTypeMapping, Long> {

    List<ReportTypeMapping> findAllByReportId(Long report);

}
