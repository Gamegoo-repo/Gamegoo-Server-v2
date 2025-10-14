package com.gamegoo.gamegoo_v2.external.riot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiotVerifyExistUserRequest {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "gameName 값은 비워둘 수 없습니다.")
    String gameName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "tag 값은 비워둘 수 없습니다.")
    String tag;

}
