package com.gamegoo.gamegoo_v2.content.report.dto.response;

import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

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
    private Long postId;
    private Boolean isPostDeleted;

    public static ReportListResponse of(Report report) {
        Long postId = null;
        Boolean isPostDeleted = null;
        if (report.getSourceBoard() != null) {
            postId = report.getSourceBoard().getId();
            isPostDeleted = report.getSourceBoard().isDeleted();
        }
        
        String reportType = report.getReportTypeMappingList().stream()
                .map(mapping -> ReportType.of(mapping.getCode()).getDescription())
                .collect(Collectors.joining(", "));
        
        return ReportListResponse.builder()
                .reportId(report.getId())
                .fromMemberId(report.getFromMember().getId())
                .fromMemberName(report.getFromMember().getGameName())
                .fromMemberTag(report.getFromMember().getTag())
                .toMemberId(report.getToMember().getId())
                .toMemberName(report.getToMember().getGameName())
                .toMemberTag(report.getToMember().getTag())
                .content(report.getContent())
                .reportType(reportType)
                .path(report.getPath().name())
                .createdAt(report.getCreatedAt())
                .postId(postId)
                .isPostDeleted(isPostDeleted)
                .build();
    }
}
