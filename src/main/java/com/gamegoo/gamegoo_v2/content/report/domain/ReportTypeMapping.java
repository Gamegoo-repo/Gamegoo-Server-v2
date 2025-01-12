package com.gamegoo.gamegoo_v2.content.report.domain;

import com.gamegoo.gamegoo_v2.core.common.BaseDateTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportTypeMapping extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_type_mapping_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(length = 2, nullable = false)
    private int code;

    public static ReportTypeMapping create(Report report, int code) {
        ReportTypeMapping reportTypeMapping = ReportTypeMapping.builder()
                .code(code)
                .build();
        reportTypeMapping.setReport(report); // 양방향 관계 설정
        return reportTypeMapping;
    }

    @Builder
    private ReportTypeMapping(Report report, int code) {
        this.report = report;
        this.code = code;
    }

    public void setReport(Report report) {
        if (this.report != null) {
            this.report.getReportTypeMappingList().remove(this);
        }
        this.report = report;
        this.report.getReportTypeMappingList().add(this);
    }

}
