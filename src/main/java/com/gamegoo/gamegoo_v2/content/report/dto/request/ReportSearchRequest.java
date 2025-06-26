package com.gamegoo.gamegoo_v2.content.report.dto.request;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportPath;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.gamegoo.gamegoo_v2.account.member.domain.BanType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSearchRequest {
    private String reportedMemberKeyword;
    private String reporterKeyword;
    private String contentKeyword;
    private List<ReportPath> reportPaths;
    private List<Integer> reportTypes;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer reportCountMin;
    private Integer reportCountMax;
    private Integer reportCountExact;
    private Boolean isDeleted;
    private Pageable pageable;
    private List<BanType> banTypes;

}
