package com.gamegoo.gamegoo_v2.external.riot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RiotJoinRequest {

    @NotBlank(message = "puuid는 비워둘 수 없습니다.")
    String puuid;

    @NotNull(message = "isAgree 값은 비워둘 수 없습니다. true/false 둘 중 하나를 반드시 포함해야합니다.")
    Boolean isAgree;

}
