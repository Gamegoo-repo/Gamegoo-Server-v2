package com.gamegoo.gamegoo_v2.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadNotificationResponse {

    Long notificationId;
    String message;

    public static ReadNotificationResponse of(Long notificationId) {
        return ReadNotificationResponse.builder()
                .notificationId(notificationId)
                .message("알림 읽음 처리 성공")
                .build();
    }

}
