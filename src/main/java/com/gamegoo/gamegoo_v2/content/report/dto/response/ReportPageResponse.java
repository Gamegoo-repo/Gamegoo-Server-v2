package com.gamegoo.gamegoo_v2.content.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportPageResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ReportListResponse> reports;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int totalPages;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private long totalElements;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int currentPage;

}
