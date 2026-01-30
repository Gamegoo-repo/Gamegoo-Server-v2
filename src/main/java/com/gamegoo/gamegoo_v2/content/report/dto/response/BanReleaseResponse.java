package com.gamegoo.gamegoo_v2.content.report.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanReleaseResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String gameName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String tag;

    @Schema(ref = "#/components/schemas/BanType", requiredMode = Schema.RequiredMode.REQUIRED)
    private BanType previousBanType;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    public static BanReleaseResponse of(Member member, BanType previousBanType) {
        return BanReleaseResponse.builder()
                .memberId(member.getId())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .previousBanType(previousBanType)
                .message("정지가 해제되었습니다.")
                .build();
    }

}
