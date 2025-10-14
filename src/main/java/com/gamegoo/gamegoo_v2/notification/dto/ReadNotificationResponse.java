package com.gamegoo.gamegoo_v2.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadNotificationResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long notificationId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message;

    public static ReadNotificationResponse of(Long notificationId) {
        return ReadNotificationResponse.builder()
                .notificationId(notificationId)
                .message("알림 읽음 처리 성공")
                .build();
    }

}
