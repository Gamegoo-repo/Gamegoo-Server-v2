package com.gamegoo.gamegoo_v2;

import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @Operation(summary = "홈 엔드포인트", description = "API 서비스 상태를 확인합니다.")
    @GetMapping("/home")
    public ApiResponse<String> home() {
        return ApiResponse.ok("Gamegoo V2 API 서비스 입니다. 환영합니다.");
    }

    @Operation(summary = "에러 테스트", description = "예외를 발생시켜 테스트합니다.")
    @GetMapping("/errortest")
    public ApiResponse<Object> error() {
        throw new GlobalException(ErrorCode._BAD_REQUEST);
    }

    @Operation(summary = "Health Check", description = "health check를 위한 API 입니다.")
    @GetMapping("/healthcheck")
    public ApiResponse<String> healthcheck() {
        return ApiResponse.ok("OK");
    }

}
