package com.gamegoo.gamegoo_v2.social.manner.dto.request;

import com.gamegoo.gamegoo_v2.core.common.annotation.NotDuplicated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MannerUpdateRequest {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotDuplicated
    @NotEmpty(message = "매너 키워드 리스트는 비워둘 수 없습니다.")
    List<Long> mannerKeywordIdList;

}
