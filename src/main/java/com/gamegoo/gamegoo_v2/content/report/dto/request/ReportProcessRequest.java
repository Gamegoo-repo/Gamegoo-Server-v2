package com.gamegoo.gamegoo_v2.content.report.dto.request;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportProcessRequest {
    
    @Schema(ref = "#/components/schemas/BanType", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "제재 유형은 필수입니다.")
    private BanType banType;
    
    private String processReason;
}
