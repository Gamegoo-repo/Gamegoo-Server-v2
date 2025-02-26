package com.gamegoo.gamegoo_v2.account.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @Email(message = "Email형식이 올바르지 않습니다.")
    @NotBlank(message = "Email은 비워둘 수 없습니다.")
    String email;

}
