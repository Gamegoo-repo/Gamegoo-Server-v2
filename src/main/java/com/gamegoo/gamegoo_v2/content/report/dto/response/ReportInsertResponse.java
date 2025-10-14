package com.gamegoo.gamegoo_v2.content.report.dto.response;

import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportInsertResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long reportId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message;

    public static ReportInsertResponse of(Report report) {
        return ReportInsertResponse.builder()
                .reportId(report.getId())
                .message("신고가 정상적으로 접수 되었습니다.")
                .build();
    }

}
