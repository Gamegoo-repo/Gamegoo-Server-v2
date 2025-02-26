package com.gamegoo.gamegoo_v2.account.member.dto.request;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IsMikeRequest {

    @NotNull(message = "mike 값은 비워둘 수 없습니다.")
    Mike mike;

}
