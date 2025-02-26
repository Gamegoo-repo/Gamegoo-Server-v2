package com.gamegoo.gamegoo_v2.account.member.dto.request;

import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PositionRequest {

    @NotNull(message = "메인 포지션은 null일 수 없습니다.")
    private Position mainP; // 메인 포지션

    @NotNull(message = "서브 포지션은 null일 수 없습니다.")
    private Position subP; // 서브 포지션

    @NotNull(message = "원하는 포지션은 null일 수 없습니다.")
    private Position wantP; // 원하는 포지션

}
