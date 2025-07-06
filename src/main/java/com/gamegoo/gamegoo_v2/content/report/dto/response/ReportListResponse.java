package com.gamegoo.gamegoo_v2.content.report.dto.response;

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
}
