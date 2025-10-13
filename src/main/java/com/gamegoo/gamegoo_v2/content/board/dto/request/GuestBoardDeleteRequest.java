package com.gamegoo.gamegoo_v2.content.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GuestBoardDeleteRequest {

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 16, message = "비밀번호는 4-16자 이내로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]*$", 
             message = "비밀번호는 영어, 숫자, 특수문자만 사용 가능합니다.")
    @Schema(description = "4-16자의 비밀번호를 입력해주세요", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
