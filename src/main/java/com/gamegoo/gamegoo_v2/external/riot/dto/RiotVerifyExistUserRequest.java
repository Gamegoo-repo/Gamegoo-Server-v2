package com.gamegoo.gamegoo_v2.external.riot.dto;

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

    @NotBlank(message = "gameName 값은 비워둘 수 없습니다.")
    String gameName;

    @NotBlank(message = "tag 값은 비워둘 수 없습니다.")
    String tag;

}
