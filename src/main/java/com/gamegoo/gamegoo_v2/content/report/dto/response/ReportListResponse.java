package com.gamegoo.gamegoo_v2.content.report.dto.response;

import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportListResponse {
    private Long reportId;
    private Long fromMemberId;
    private String fromMemberName;
    private String fromMemberTag;
    private Long toMemberId;
    private String toMemberName;
    private String toMemberTag;
    private String content;
    private String reportType;
    private String path;
    private LocalDateTime createdAt;

    public static ReportListResponse of(Report report) {
        return ReportListResponse.builder()
                .reportId(report.getId())
                .fromMemberId(report.getFromMember().getId())
                .fromMemberName(report.getFromMember().getGameName())
                .fromMemberTag(report.getFromMember().getTag())
                .toMemberId(report.getToMember().getId())
                .toMemberName(report.getToMember().getGameName())
                .toMemberTag(report.getToMember().getTag())
                .content(report.getContent())
                .path(report.getPath().name())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
