package com.gamegoo.gamegoo_v2.content.report.dto.response;

import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReportListResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reportId;
    private Long fromMemberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String fromMemberName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String fromMemberTag;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toMemberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String toMemberName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String toMemberTag;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportType;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String path;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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

        Long fromMemberId = null;
        String fromMemberName = "비회원";
        String fromMemberTag = "#";
        if (report.getFromMember() != null) {
            fromMemberId = report.getFromMember().getId();
            fromMemberName = report.getFromMember().getGameName();
            fromMemberTag = report.getFromMember().getTag();
        }

        String reportType = report.getReportTypeMappingList().stream()
                .map(mapping -> ReportType.of(mapping.getCode()).getDescription())
                .collect(Collectors.joining(", "));

        return ReportListResponse.builder()
                .reportId(report.getId())
                .fromMemberId(fromMemberId)
                .fromMemberName(fromMemberName)
                .fromMemberTag(fromMemberTag)
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
