package com.gamegoo.gamegoo_v2.account.member.dto.request;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IsMikeRequest {

    @Schema(ref = "#/components/schemas/Mike", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "mike 값은 비워둘 수 없습니다.")
    Mike mike;

}
