package com.gamegoo.gamegoo_v2.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SystemFlagRequest {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "flag는 1 이상의 값이어야 합니다.")
    @Max(value = 2, message = "flag는 2 이하의 값이어야 합니다.")
    @NotNull(message = "flag는 필수 값 입니다.")
    Integer flag;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "boardId는 필수 값 입니다.")
    Long boardId;

}
