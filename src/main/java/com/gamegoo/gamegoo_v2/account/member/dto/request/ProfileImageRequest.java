package com.gamegoo.gamegoo_v2.account.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProfileImageRequest {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "프로필 이미지의 값은 1이상이어야 합니다.")
    @Max(value = 8, message = "프로필 이미지의 값은 8이하이어야 합니다.")
    @NotNull(message = "profileImage 값은 비워둘 수 없습니다.")
    Integer profileImage;


}
