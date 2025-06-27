package com.gamegoo.gamegoo_v2.content.report.repository;

import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {

    boolean existsByFromMemberIdAndToMemberIdAndCreatedAtBetween(Long fromMemberId, Long toMemberId,
                                                                 LocalDateTime startOfDay, LocalDateTime endOfDay);

}
