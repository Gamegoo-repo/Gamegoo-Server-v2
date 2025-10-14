package com.gamegoo.gamegoo_v2.content.report.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportProcessResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reportId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetMemberId;
    @Schema(ref = "#/components/schemas/BanType", requiredMode = Schema.RequiredMode.REQUIRED)
    private BanType appliedBanType;
    private LocalDateTime banExpireAt;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    public static ReportProcessResponse of(Long reportId, Long targetMemberId, BanType banType, LocalDateTime banExpireAt) {
        return ReportProcessResponse.builder()
                .reportId(reportId)
                .targetMemberId(targetMemberId)
                .appliedBanType(banType)
                .banExpireAt(banExpireAt)
                .message("신고 처리가 완료되었습니다.")
                .build();
    }
}
