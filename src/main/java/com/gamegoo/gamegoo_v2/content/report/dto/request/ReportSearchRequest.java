package com.gamegoo.gamegoo_v2.content.report.dto.request;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportPath;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportSortOrder;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSearchRequest {
    private String reportedMemberKeyword;
    private String reporterKeyword;
    private String contentKeyword;
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/ReportPath"))
    private List<ReportPath> reportPaths;
    private List<Integer> reportTypes;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer reportCountMin;
    private Integer reportCountMax;
    private Integer reportCountExact;
    private Boolean isDeleted;
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/BanType"))
    private List<BanType> banTypes;
    @Schema(ref = "#/components/schemas/ReportSortOrder")
    private ReportSortOrder sortOrder;
}
